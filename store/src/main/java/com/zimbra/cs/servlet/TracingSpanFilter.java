package com.zimbra.cs.servlet;

import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class TracingSpanFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpReq = (HttpServletRequest) req;

		Span currentSpan = Span.current();

		if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
			String path = httpReq.getRequestURI();
			currentSpan.updateName(path);
		}

		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) { }

	@Override
	public void destroy() { }
}
