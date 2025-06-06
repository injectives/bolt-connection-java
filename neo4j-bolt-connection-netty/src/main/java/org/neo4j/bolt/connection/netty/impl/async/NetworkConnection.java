/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.bolt.connection.netty.impl.async;

import static org.neo4j.bolt.connection.netty.impl.async.connection.ChannelAttributes.setClosing;
import static org.neo4j.bolt.connection.netty.impl.async.connection.ChannelAttributes.setTerminationReason;
import static org.neo4j.bolt.connection.netty.impl.util.LockUtil.executeWithLock;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoop;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.neo4j.bolt.connection.BoltServerAddress;
import org.neo4j.bolt.connection.LoggingProvider;
import org.neo4j.bolt.connection.netty.impl.async.connection.ChannelAttributes;
import org.neo4j.bolt.connection.netty.impl.async.inbound.ConnectionReadTimeoutHandler;
import org.neo4j.bolt.connection.netty.impl.async.inbound.InboundMessageDispatcher;
import org.neo4j.bolt.connection.netty.impl.handlers.NoOpResponseHandler;
import org.neo4j.bolt.connection.netty.impl.messaging.BoltProtocol;
import org.neo4j.bolt.connection.netty.impl.messaging.Message;
import org.neo4j.bolt.connection.netty.impl.messaging.request.GoodbyeMessage;
import org.neo4j.bolt.connection.netty.impl.spi.Connection;
import org.neo4j.bolt.connection.netty.impl.spi.ResponseHandler;

/**
 * This connection represents a simple network connection to a remote server. It wraps a channel obtained from a connection pool. The life cycle of this
 * connection start from the moment the channel is borrowed out of the pool and end at the time the connection is released back to the pool.
 */
public class NetworkConnection implements Connection {
    private final System.Logger log;
    private final Lock lock;
    private final Channel channel;
    private final InboundMessageDispatcher messageDispatcher;
    private final String serverAgent;
    private final BoltServerAddress serverAddress;
    private final boolean telemetryEnabled;
    private final boolean ssrEnabled;
    private final BoltProtocol protocol;

    private final Duration defaultReadTimeout;
    private Duration readTimeout;

    private ChannelHandler connectionReadTimeoutHandler;

    public NetworkConnection(Channel channel, LoggingProvider logging) {
        this.log = logging.getLog(getClass());
        this.lock = new ReentrantLock();
        this.channel = channel;
        this.messageDispatcher = ChannelAttributes.messageDispatcher(channel);
        this.serverAgent = ChannelAttributes.serverAgent(channel);
        this.serverAddress = ChannelAttributes.serverAddress(channel);
        this.telemetryEnabled = ChannelAttributes.telemetryEnabled(channel);
        this.ssrEnabled = ChannelAttributes.ssrEnabled(channel);
        this.protocol = BoltProtocol.forChannel(channel);
        this.defaultReadTimeout = ChannelAttributes.connectionReadTimeout(channel)
                .map(Duration::ofSeconds)
                .orElse(null);
        this.readTimeout = defaultReadTimeout;
    }

    @Override
    public boolean isOpen() {
        return executeWithLock(lock, channel::isOpen);
    }

    @Override
    public void enableAutoRead() {
        if (isOpen()) {
            setAutoRead(true);
        }
    }

    @Override
    public void disableAutoRead() {
        if (isOpen()) {
            setAutoRead(false);
        }
    }

    @Override
    public CompletionStage<Void> write(Message message, ResponseHandler handler) {
        return writeMessageInEventLoop(message, handler);
    }

    @Override
    public CompletionStage<Void> flush() {
        var future = new CompletableFuture<Void>();
        channel.eventLoop().execute(() -> {
            channel.flush();
            future.complete(null);
        });
        return future;
    }

    @Override
    public boolean isTelemetryEnabled() {
        return telemetryEnabled;
    }

    @Override
    public boolean isSsrEnabled() {
        return ssrEnabled;
    }

    @Override
    public String serverAgent() {
        return serverAgent;
    }

    @Override
    public BoltServerAddress serverAddress() {
        return serverAddress;
    }

    @Override
    public BoltProtocol protocol() {
        return protocol;
    }

    @Override
    public CompletionStage<Void> forceClose(String reason) {
        var fut = new CompletableFuture<Void>();
        eventLoop().execute(() -> {
            setTerminationReason(channel, reason);
            setClosing(channel);
            channel.close().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    fut.complete(null);
                } else {
                    var cause = future.cause();
                    if (cause == null) {
                        cause = new IllegalStateException("Unexpected state");
                    }
                    fut.completeExceptionally(cause);
                }
            });
        });
        return fut;
    }

    @Override
    public CompletionStage<Void> close() {
        var closeFuture = new CompletableFuture<Void>();
        writeMessageInEventLoop(GoodbyeMessage.GOODBYE, new NoOpResponseHandler())
                .thenCompose(ignored -> flush())
                .whenComplete((ignored, throwable) -> {
                    if (throwable == null) {
                        setClosing(channel);
                        this.channel.close().addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                closeFuture.complete(null);
                            } else {
                                closeFuture.completeExceptionally(future.cause());
                            }
                        });
                    } else {
                        closeFuture.completeExceptionally(throwable);
                    }
                });
        return closeFuture;
    }

    @Override
    public EventLoop eventLoop() {
        return channel.eventLoop();
    }

    @Override
    public Optional<Duration> defaultReadTimeoutMillis() {
        return Optional.ofNullable(defaultReadTimeout);
    }

    @Override
    public void setReadTimeout(Duration duration) {
        if (!channel.eventLoop().inEventLoop()) {
            throw new IllegalStateException("This method may only be called in the EventLoop");
        }

        if (duration != null && duration.toMillis() > 0) {
            // only values greater than zero milliseconds are supported
            this.readTimeout = duration;
        } else {
            this.readTimeout = this.defaultReadTimeout;
        }
    }

    private CompletionStage<Void> writeMessageInEventLoop(Message message, ResponseHandler handler) {
        var future = new CompletableFuture<Void>();
        Runnable runnable = () -> {
            if (messageDispatcher.fatalErrorOccurred() && GoodbyeMessage.GOODBYE.equals(message)) {
                future.complete(null);
                handler.onSuccess(Collections.emptyMap());
                setClosing(channel);
                channel.close();
                return;
            }
            messageDispatcher.enqueue(handler);
            channel.write(message).addListener(writeFuture -> {
                if (writeFuture.isSuccess()) {
                    registerConnectionReadTimeout(channel);
                } else {
                    future.completeExceptionally(writeFuture.cause());
                }
            });
            future.complete(null);
        };
        if (channel.eventLoop().inEventLoop()) {
            runnable.run();
        } else {
            channel.eventLoop().execute(runnable);
        }
        return future;
    }

    private void setAutoRead(boolean value) {
        channel.config().setAutoRead(value);
    }

    private void registerConnectionReadTimeout(Channel channel) {
        if (!channel.eventLoop().inEventLoop()) {
            throw new IllegalStateException("This method may only be called in the EventLoop");
        }

        if (this.readTimeout != null && connectionReadTimeoutHandler == null) {
            connectionReadTimeoutHandler =
                    new ConnectionReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS);
            channel.pipeline().addFirst(connectionReadTimeoutHandler);
            log.log(System.Logger.Level.DEBUG, "Added ConnectionReadTimeoutHandler");
            messageDispatcher.setBeforeLastHandlerHook(() -> {
                channel.pipeline().remove(connectionReadTimeoutHandler);
                connectionReadTimeoutHandler = null;
                messageDispatcher.setBeforeLastHandlerHook(null);
                log.log(System.Logger.Level.DEBUG, "Removed ConnectionReadTimeoutHandler");
            });
        }
    }
}
