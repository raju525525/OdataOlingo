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
package org.apache.olingo.server.core.batch.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.batch.BatchException;
import org.apache.olingo.server.api.batch.BatchOperation;
import org.apache.olingo.server.api.batch.BatchRequestPart;
import org.apache.olingo.server.api.batch.ODataResponsePart;
import org.apache.olingo.server.api.processor.BatchProcessor;
import org.apache.olingo.server.core.ODataHandler;
import org.apache.olingo.server.core.batch.parser.BatchParserCommon;
import org.apache.olingo.server.core.batch.transformator.HttpRequestStatusLine.ODataURI;

public class BatchPartHandler {

  private ODataHandler oDataHandler;
  private BatchProcessor batchProcessor;
  private BatchOperation batchOperation;
  private Map<BatchRequestPart, UriMapping> uriMapping = new HashMap<BatchRequestPart, UriMapping>();
  
  public BatchPartHandler(final ODataHandler oDataHandler, final BatchProcessor processor,
      final BatchOperation batchOperation) {
    this.oDataHandler = oDataHandler;
    this.batchProcessor = processor;
    this.batchOperation = batchOperation;
  }

  public ODataResponse handleODataRequest(ODataRequest request, BatchRequestPart requestPart) throws BatchException {
    final ODataResponse response;
    
    if(requestPart.isChangeSet()) {
      final UriMapping mapping = replaceReference(request, requestPart);

      response = oDataHandler.process(request);
       
      final String resourceUri = getODataPath(request, response);
      final String contentId = request.getHeader(BatchParserCommon.HTTP_CONTENT_ID);

      mapping.addMapping(contentId, resourceUri);
    } else {
      response = oDataHandler.process(request);
    }
    
    final String contentId = request.getHeader(BatchParserCommon.HTTP_CONTENT_ID);
    if(contentId != null) {
      response.setHeader(BatchParserCommon.HTTP_CONTENT_ID, contentId);
    }
    
    return  response;
  }

  private String getODataPath(ODataRequest request, ODataResponse response) throws BatchException {
    String resourceUri = null;
    
    if(request.getMethod() == HttpMethod.POST) {
      // Create entity
      // The URI of the new resource will be generated by the server and published in the location header
      ODataURI uri = new ODataURI(response.getHeaders().get(HttpHeader.LOCATION), request.getRawBaseUri());
      resourceUri = uri.getRawODataPath();
    } else {
      // Update, Upsert (PUT, PATCH, Delete)
      // These methods still addresses a given URI, so we use the URI given by the request
      resourceUri = request.getRawODataPath();
    }
    
    return resourceUri;
  }

  private UriMapping replaceReference(ODataRequest request, BatchRequestPart requestPart) {
    final UriMapping mapping = getUriMappingOrDefault(requestPart);
    final String reference = BatchChangeSetSorter.getReferenceInURI(request);
    
    if(reference != null) {
      final String replacement = mapping.getUri(reference);
      
      if(replacement != null) {
        BatchChangeSetSorter.replaceContentIdReference(request, reference, replacement);
      } else {
        throw new ODataRuntimeException("Required Content-Id for reference \"" + reference + "\" not found.");
      }
    }
    
    return mapping;
  }
  
  private UriMapping getUriMappingOrDefault(final BatchRequestPart requestPart) {
    UriMapping mapping = uriMapping.get(requestPart);
    
    if(mapping == null) {
      mapping = new UriMapping();
    }
    uriMapping.put(requestPart, mapping);
    
    return mapping;
  }
  
  public ODataResponsePart handleBatchRequest(BatchRequestPart request) throws BatchException {
    if (request.isChangeSet()) {
      return handleChangeSet(request);
    } else {
      final List<ODataResponse> responses = new ArrayList<ODataResponse>();
      responses.add(handleODataRequest(request.getRequests().get(0), request));
      
      return new ODataResponsePartImpl(responses, false);
    }
  }

  private ODataResponsePart handleChangeSet(BatchRequestPart request) throws BatchException {
    final List<ODataResponse> responses = new ArrayList<ODataResponse>();
    final BatchChangeSetSorter sorter = new BatchChangeSetSorter(request.getRequests());

    responses.addAll(batchProcessor.executeChangeSet(batchOperation, sorter.getOrderdRequests(), request));
    
    return new ODataResponsePartImpl(responses, true);
  }

}
