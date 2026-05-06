package com.zimbra.cs.servlet;

import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

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
