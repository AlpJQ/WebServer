package com.sinjinsong.webserver.core.network.dispatcher.bio;

import com.sinjinsong.webserver.core.exception.RequestInvalidException;
import com.sinjinsong.webserver.core.exception.base.ServletException;
import com.sinjinsong.webserver.core.network.dispatcher.AbstractDispatcher;
import com.sinjinsong.webserver.core.network.handler.bio.BioRequestHandler;
import com.sinjinsong.webserver.core.network.wrapper.SocketWrapper;
import com.sinjinsong.webserver.core.network.wrapper.bio.BioSocketWrapper;
import com.sinjinsong.webserver.core.request.Request;
import com.sinjinsong.webserver.core.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author sinjinsong
 * @date 2018/5/4
 * 请求分发线程，往往对应一个线程池，负责读取Request以及将RequestHandler放入线程池中执行
 *
 *
 * 负责先读取请求，然后构造RequestHandler然后放到线程池中执行。
 * 这里遇到了一个坑，本来是打算把读取也放到RequestHandler中并发执行的，但是在测试的时候发现
 * 线程池中有多个线程都在执行读取，就是一个客户端请求被分到了多个线程中执行，并且Request无法
 * 读取完整。所以只能把客户端的读取放到单线程执行的Dispatcher中。
 * 还有一个坑是在读取Request完之后不能把inputStream关掉，否则会把socket（客户端连接）也
 * 关掉，导致后面的响应写回时抛出Socket已经关闭的异常。
 */
@Slf4j
public class BioDispatcher extends AbstractDispatcher {

    //分发请求
    @Override
    public void doDispatch(SocketWrapper socketWrapper) {
        BioSocketWrapper bioSocketWrapper = (BioSocketWrapper) socketWrapper;
        Socket socket = bioSocketWrapper.getSocket();
        Request request = null;
        Response response = null;
        try {
            BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
            byte[] buf = null;
            try {
                buf = new byte[bin.available()];
                int len = bin.read(buf);
                if (len <= 0) {
                    throw new RequestInvalidException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 这里不要把in关掉，把in关掉就等同于把socket关掉
            //解析请求
            response = new Response();
            request = new Request(buf);
            pool.execute(new BioRequestHandler(socketWrapper, servletContext, exceptionHandler, resourceHandler, request, response));
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
