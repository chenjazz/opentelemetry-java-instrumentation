/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.tomcat.common;

import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesExtractor;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.MessageBytes;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TomcatHttpAttributesExtractor
    extends HttpServerAttributesExtractor<Request, Response> {

  @Override
  protected String method(Request request) {
    return request.method().toString();
  }

  @Override
  protected @Nullable String target(Request request) {
    String target = request.requestURI().toString();
    String queryString = request.queryString().toString();
    if (queryString != null) {
      target += "?" + queryString;
    }
    return target;
  }

  @Override
  protected @Nullable String host(Request request) {
    return request.serverName().toString() + ":" + request.getServerPort();
  }

  @Override
  protected @Nullable String scheme(Request request) {
    MessageBytes schemeMessageBytes = request.scheme();
    return schemeMessageBytes.isNull() ? "http" : schemeMessageBytes.toString();
  }

  @Override
  protected @Nullable String userAgent(Request request) {
    return request.getHeader("User-Agent");
  }

  @Override
  protected @Nullable Long requestContentLength(Request request, @Nullable Response response) {
    /*
    long contentLength = request.getContentLengthLong();
    return contentLength != -1 ? contentLength : null;
     */
    return null;
  }

  @Override
  protected @Nullable Long requestContentLengthUncompressed(
      Request request, @Nullable Response response) {
    return null;
  }

  @Override
  protected @Nullable String flavor(Request request, @Nullable Response response) {
    String flavor = request.protocol().toString();
    if (flavor != null) {
      // remove HTTP/ prefix to comply with semantic conventions
      if (flavor.startsWith("HTTP/")) {
        flavor = flavor.substring("HTTP/".length());
      }
    }
    return flavor;
  }

  @Override
  protected @Nullable Integer statusCode(Request request, Response response) {
    return response.getStatus();
  }

  @Override
  protected @Nullable Long responseContentLength(Request request, Response response) {
    /*
    long contentLength = response.getContentLengthLong();
    return contentLength != -1 ? contentLength : null;
     */
    return null;
  }

  @Override
  protected @Nullable Long responseContentLengthUncompressed(Request request, Response response) {
    return null;
  }

  @Override
  protected @Nullable String route(Request request) {
    return null;
  }

  @Override
  protected @Nullable String serverName(Request request, @Nullable Response response) {
    // return request.serverName().toString();
    return null;
  }
}