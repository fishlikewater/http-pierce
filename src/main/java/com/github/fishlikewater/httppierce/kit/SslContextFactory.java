package com.github.fishlikewater.httppierce.kit;

import cn.hutool.core.io.FileUtil;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年10月20日 9:21
 **/
@SuppressWarnings("unused")
@Slf4j
public class SslContextFactory {

    private SslContextFactory() {}

    private static final String PROTOCOL = "TLS";

    private static SSLContext SERVER_CONTEXT;

    private static SslContext openSslContext;

    private static SSLContext CLIENT_CONTEXT;

    private static SslContext openSslClientContext;

    public static SSLContext getServerContext(String pkPath, String passwd) {
        if (SERVER_CONTEXT != null) {
            return SERVER_CONTEXT;
        }
        // 密钥管理器
        final KeyManagerFactory kmf = handlerKmf(pkPath, passwd);
        try {
            // 获取安全套接字协议（TLS协议）的对象
            SERVER_CONTEXT = SSLContext.getInstance(PROTOCOL);
            // 初始化此上下文
            // 参数一：认证的密钥 参数二：对等信任认证 参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
            assert kmf != null;
            SERVER_CONTEXT.init(kmf.getKeyManagers(), null, null);
            return SERVER_CONTEXT;
        } catch (Exception e) {
            throw new Error("Failed to initialize the server-side SSLContext", e);
        }
    }

    public static SslContext getOpenSslServerContext(String pkPath, String passwd) {
        if (openSslContext != null) {
            return openSslContext;
        }
        final KeyManagerFactory kmf = handlerKmf(pkPath, passwd);
        try {
            assert kmf != null;
            openSslContext = SslContextBuilder.forServer(kmf)
                    .sslProvider(SslProvider.OPENSSL).build();
            return openSslContext;
        } catch (Exception e) {
            throw new Error("Failed to initialize the server-side SslContext", e);
        }
    }


    public static SSLContext getClientContext(String pkPath, String passwd) {
        if (CLIENT_CONTEXT != null) {
            return CLIENT_CONTEXT;
        }
        final TrustManagerFactory tf = handlerTf(pkPath, passwd);
        try {
            CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
            // 设置信任证书
            CLIENT_CONTEXT.init(null, tf == null ? null : tf.getTrustManagers(), null);
            return CLIENT_CONTEXT;
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext");
        }
    }

    public static SslContext getOpenSslClientContext(String pkPath, String passwd) {

        if (openSslClientContext != null) {
            return openSslClientContext;
        }
        final TrustManagerFactory tf = handlerTf(pkPath, passwd);
        try {
            assert tf != null;
            openSslClientContext = SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL).trustManager(tf).build();
            return openSslClientContext;
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext");
        }

    }


    public static SSLContext getServerContext(String pkPath, String caPath, String passwd) {
        if (SERVER_CONTEXT != null) {
            return SERVER_CONTEXT;
        }
        try {
            // 密钥管理器
            final KeyManagerFactory kmf = handlerKmf(pkPath, passwd);
            final TrustManagerFactory tf = handlerTf(caPath, passwd);
            SERVER_CONTEXT = SSLContext.getInstance(PROTOCOL);
            // 初始化此上下文
            // 参数一：认证的密钥 参数二：对等信任认证 参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
            // 单向认证？无需验证客户端证书
            assert kmf != null;
            if (tf == null) {
                SERVER_CONTEXT.init(kmf.getKeyManagers(), null, null);
            }
            // 双向认证，需要验证客户端证书
            else {
                SERVER_CONTEXT.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
            }
            return SERVER_CONTEXT;
        } catch (Exception e) {
            throw new Error("Failed to initialize the server-side SSLContext", e);
        }
    }

    public static SslContext getOpenSslServerContext(String pkPath, String caPath, String passwd) {
        if (openSslContext != null) {
            return openSslContext;
        }
        try {
            // 密钥管理器
            final KeyManagerFactory kmf = handlerKmf(pkPath, passwd);
            final TrustManagerFactory tf = handlerTf(caPath, passwd);
            openSslContext = SslContextBuilder.forServer(kmf).trustManager(tf).sslProvider(SslProvider.OPENSSL).build();
            return openSslContext;
        } catch (Exception e) {
            log.error("ssl加载错误", e);
        }
        return null;
    }


    public static SSLContext getClientContext(String pkPath, String caPath,
                                              String passwd) {
        if (CLIENT_CONTEXT != null) {
            return CLIENT_CONTEXT;
        }
        try {
            final KeyManagerFactory kmf = handlerKmf(pkPath, passwd);
            final TrustManagerFactory tf = handlerTf(caPath, passwd);
            CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
            // 初始化此上下文
            // 参数一：认证的密钥 参数二：对等信任认证 参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
            assert kmf != null;
            assert tf != null;
            CLIENT_CONTEXT.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext", e);
        }
        return CLIENT_CONTEXT;
    }

    public static SslContext getOpenSslClientContext(String pkPath, String caPath, String passwd) {
        if (openSslClientContext != null) {
            return openSslClientContext;
        }
        try {
            // 密钥管理器
            final KeyManagerFactory kmf = handlerKmf(pkPath, passwd);
            final TrustManagerFactory tf = handlerTf(caPath, passwd);
            openSslClientContext = SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL).keyManager(kmf).trustManager(tf).build();
            return openSslClientContext;
        } catch (Exception e) {
            log.error("ssl加载错误", e);
        }
        return null;
    }

    /**
     * Description:
     */
    public static SSLEngine getSslServerEngine(SslConfig sslConfig) {
        SSLEngine sslEngine;
        if (sslConfig.isNeedClientAuth()) {
            sslEngine = getServerContext(sslConfig.getPkPath(), sslConfig.getCaPath(), sslConfig.getPasswd()).createSSLEngine();
        } else {
            sslEngine = getServerContext(sslConfig.getPkPath(), sslConfig.getPasswd()).createSSLEngine();
        }

        sslEngine.setUseClientMode(false);
        sslEngine.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        // false为单向认证，true为双向认证
        sslEngine.setNeedClientAuth(sslConfig.isNeedClientAuth());
        return sslEngine;
    }

    public static SSLEngine getOpenSslServerEngine(SslConfig sslConfig, ByteBufAllocator alloc) {
        SSLEngine sslEngine;
        if (sslConfig.isNeedClientAuth()) {
            sslEngine = Objects.requireNonNull(getOpenSslServerContext(sslConfig.getPkPath(), sslConfig.getCaPath(), sslConfig.getPasswd())).newEngine(alloc);
        } else {
            sslEngine = getOpenSslServerContext(sslConfig.getPkPath(), sslConfig.getPasswd()).newEngine(alloc);
        }
        sslEngine.setUseClientMode(false);
        sslEngine.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        // false为单向认证，true为双向认证
        sslEngine.setNeedClientAuth(sslConfig.isNeedClientAuth());
        return sslEngine;
    }

    public static SSLEngine getSslClientEngine(String pkPath, String caPath, String passwd, boolean isNeedClientAuth) {
        SSLEngine sslEngine;
        if (isNeedClientAuth) {
            sslEngine = getClientContext(pkPath, caPath, passwd).createSSLEngine();
        } else {
            sslEngine = getClientContext(pkPath, passwd).createSSLEngine();
        }
        sslEngine.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }

    public static SSLEngine getOpenSslClientEngine(String pkPath, String caPath, String passwd, ByteBufAllocator alloc, boolean isNeedClientAuth) {

        SSLEngine sslEngine;
        if (isNeedClientAuth) {
            sslEngine = Objects.requireNonNull(getOpenSslClientContext(pkPath, caPath, passwd)).newEngine(alloc);
        } else {
            sslEngine = getOpenSslClientContext(pkPath, passwd).newEngine(alloc);
        }
        sslEngine.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }


    public static SslHandler getSslHandler(SslConfig sslConfig) {

        if (sslConfig != null && sslConfig.isEnable()) {
            return new SslHandler(getSslServerEngine(sslConfig));
        } else {
            return null;
        }
    }

    public static SslHandler getOpenSslHandler(SslConfig sslConfig, ByteBufAllocator alloc) {

        if (sslConfig != null && sslConfig.isEnable()) {
            return new SslHandler(getOpenSslServerEngine(sslConfig, alloc));
        } else {
            return null;
        }
    }

    public static SslHandler getSslClientHandler(SslConfig sslConfig) {
        if (sslConfig != null && sslConfig.isEnable()) {
            return new SslHandler(getSslClientEngine(sslConfig.getPkPath(), sslConfig.getCaPath(), sslConfig.getPasswd(), sslConfig.isNeedClientAuth()));
        } else {
            return null;
        }
    }

    public static SslHandler getOpenSslClientHandler(SslConfig sslConfig, ByteBufAllocator alloc) {
        if (sslConfig != null && sslConfig.isEnable()) {
            return new SslHandler(getOpenSslClientEngine(sslConfig.getPkPath(), sslConfig.getCaPath(), sslConfig.getPasswd(), alloc, sslConfig.isNeedClientAuth()));
        } else {
            return null;
        }
    }

    private static KeyManagerFactory handlerKmf(String pkPath, String passwd) {
        KeyManagerFactory kmf = null;
        if (pkPath != null) {
            try (InputStream in = new FileInputStream(FileUtil.file(pkPath))) {
                // 密钥库KeyStore
                KeyStore ks = KeyStore.getInstance("JKS");
                // 加载服务端的KeyStore ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
                ks.load(in, passwd.toCharArray());
                kmf = KeyManagerFactory.getInstance("SunX509");
                // 初始化密钥管理器
                kmf.init(ks, passwd.toCharArray());

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize the server-side SSLContext", e);
            }
        }
        return kmf;
    }

    private static TrustManagerFactory handlerTf(String pkPath, String passwd) {
        TrustManagerFactory tf = null;
        if (pkPath != null) {
            try (InputStream tIn = new FileInputStream(FileUtil.file(pkPath))) {
                // 信任库
                // 密钥库KeyStore
                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(tIn, passwd.toCharArray());
                tf = TrustManagerFactory.getInstance("SunX509");
                // 初始化信任库
                tf.init(tks);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize the client-side SSLContext");
            }
        }
        return tf;
    }
}
