package com.ducnh.socket.io.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;

public class WrongUrlHandler extends ChannelInboundHandlerAdapter{
	
	private static final Logger log = LoggerFactory.getLogger(WrongUrlHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest  req = (FullHttpRequest) msg;
			Channel channel = ctx.channel();
			QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
			
			
			HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
			ChannelFuture f = channel.writeAndFlush(res);
			f.addListener(ChannelFutureListener.CLOSE);
			req.release();
			log.warn("Blocked wrong socket.io-context request! url: {}, params: {}, ip: {}", queryDecoder.path(), queryDecoder.parameters(), channel.remoteAddress());
			return;
		}
		
		super.channelRead(ctx, msg);
	}
}
