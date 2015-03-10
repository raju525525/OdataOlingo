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
package org.apache.olingo.commons.core.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Link;

public class LinkImpl extends AbstractAnnotatedObject implements Link {

  private String title;
  private String rel;
  private String href;
  private String type;
  private String mediaETag;
  private String bindingLink;
  private List<String> bindingLinks = new ArrayList<String>();

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(final String title) {
    this.title = title;
  }

  @Override
  public String getRel() {
    return rel;
  }

  @Override
  public void setRel(final String rel) {
    this.rel = rel;
  }

  @Override
  public String getHref() {
    return href;
  }

  @Override
  public void setHref(final String href) {
    this.href = href;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(final String type) {
    this.type = type;
  }

  @Override
  public String getMediaETag() {
    return mediaETag;
  }

  @Override
  public void setMediaETag(final String mediaETag) {
    this.mediaETag = mediaETag;
  }

  @Override
  public String getBindingLink() {
    return bindingLink;
  }

  @Override
  public List<String> getBindingLinks() {
    return bindingLinks;
  }

  @Override
  public void setBindingLink(String bindingLink) {
    this.bindingLink = bindingLink;
  }

  @Override
  public void setBindingLinks(List<String> bindingLinks) {
    this.bindingLinks = bindingLinks;
  }
}
