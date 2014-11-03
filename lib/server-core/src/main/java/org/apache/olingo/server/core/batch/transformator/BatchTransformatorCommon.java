/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.batch.transformator;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.http.HttpContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.batch.BatchException;
import org.apache.olingo.server.api.batch.BatchException.MessageKeys;
import org.apache.olingo.server.core.batch.parser.BatchParserCommon;
import org.apache.olingo.server.core.batch.parser.Header;
import org.apache.olingo.server.core.batch.parser.HeaderField;

public class BatchTransformatorCommon {

  public static void validateContentType(final Header headers, final Pattern pattern) throws BatchException {
    List<String> contentTypes = headers.getHeaders(HttpHeader.CONTENT_TYPE);

    if (contentTypes.size() == 0) {
      throw new BatchException("Missing content type", MessageKeys.MISSING_CONTENT_TYPE, headers.getLineNumber());
    }
    if (!headers.isHeaderMatching(HttpHeader.CONTENT_TYPE, pattern)) {

      throw new BatchException("Invalid content type", MessageKeys.INVALID_CONTENT_TYPE,
          HttpContentType.MULTIPART_MIXED + " or " + HttpContentType.APPLICATION_HTTP);
    }
  }

  public static void validateContentTransferEncoding(Header headers) throws BatchException {
    final HeaderField contentTransferField = headers.getHeaderField(BatchParserCommon.HTTP_CONTENT_TRANSFER_ENCODING);

    if (contentTransferField != null) {
      final List<String> contentTransferValues = contentTransferField.getValues();
      if (contentTransferValues.size() == 1) {
        String encoding = contentTransferValues.get(0);

        if (!BatchParserCommon.BINARY_ENCODING.equalsIgnoreCase(encoding)) {
          throw new BatchException("Invalid content transfer encoding", MessageKeys.INVALID_CONTENT_TRANSFER_ENCODING,
              headers.getLineNumber());
        }
      } else {
        throw new BatchException("Invalid header", MessageKeys.INVALID_HEADER, headers.getLineNumber());
      }
    } else {
      throw new BatchException("Missing mandatory content transfer encoding",
          MessageKeys.MISSING_CONTENT_TRANSFER_ENCODING,
          headers.getLineNumber());
    }
  }

  public static int getContentLength(Header headers) throws BatchException {
    final HeaderField contentLengthField = headers.getHeaderField(HttpHeader.CONTENT_LENGTH);

    if (contentLengthField != null && contentLengthField.getValues().size() == 1) {
      final List<String> contentLengthValues = contentLengthField.getValues();

      try {
        int contentLength = Integer.parseInt(contentLengthValues.get(0));

        if (contentLength < 0) {
          throw new BatchException("Invalid content length", MessageKeys.INVALID_CONTENT_LENGTH, contentLengthField
              .getLineNumber());
        }

        return contentLength;
      } catch (NumberFormatException e) {
        throw new BatchException("Invalid header", MessageKeys.INVALID_HEADER, contentLengthField.getLineNumber());
      }
    }

    return -1;
  }
}
