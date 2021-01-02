/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.base;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tuple.Tuple2;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPApplicationDefinitionAssetsBundle {

    private String assetsFolderPath;

    private List<Tuple2<String, String>> assetsRelativePathAndTextContent = new ArrayList<>();
    private List<Tuple2<String, byte[]>> assetsRelativePathAndBinaryContent = new ArrayList<>();

    public IPApplicationDefinitionAssetsBundle() {
    }

    public IPApplicationDefinitionAssetsBundle(String assetsFolderPath) {
        this.assetsFolderPath = assetsFolderPath;
    }

    public IPApplicationDefinitionAssetsBundle addAssetContent(String assetRelativePath, byte[] content) {
        assetsRelativePathAndBinaryContent.add(new Tuple2<>(assetRelativePath, content));
        return this;
    }

    public IPApplicationDefinitionAssetsBundle addAssetContent(String assetRelativePath, String content) {
        assetsRelativePathAndTextContent.add(new Tuple2<>(assetRelativePath, content));
        return this;
    }

    public IPApplicationDefinitionAssetsBundle addAssetResource(String assetRelativePath, String sourceResource) {
        String content = ResourceTools.getResourceAsString(sourceResource);
        return addAssetContent(assetRelativePath, content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IPApplicationDefinitionAssetsBundle other = (IPApplicationDefinitionAssetsBundle) obj;
        if (assetsFolderPath == null) {
            if (other.assetsFolderPath != null) {
                return false;
            }
        } else if (!assetsFolderPath.equals(other.assetsFolderPath)) {
            return false;
        }
        if (assetsRelativePathAndBinaryContent == null) {
            if (other.assetsRelativePathAndBinaryContent != null) {
                return false;
            }
        } else if (!assetsRelativePathAndBinaryContent.equals(other.assetsRelativePathAndBinaryContent)) {
            return false;
        }
        if (assetsRelativePathAndTextContent == null) {
            if (other.assetsRelativePathAndTextContent != null) {
                return false;
            }
        } else if (!assetsRelativePathAndTextContent.equals(other.assetsRelativePathAndTextContent)) {
            return false;
        }
        return true;
    }

    public String getAssetsFolderPath() {
        return assetsFolderPath;
    }

    public List<Tuple2<String, byte[]>> getAssetsRelativePathAndBinaryContent() {
        return assetsRelativePathAndBinaryContent;
    }

    public List<Tuple2<String, String>> getAssetsRelativePathAndTextContent() {
        return assetsRelativePathAndTextContent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assetsFolderPath == null) ? 0 : assetsFolderPath.hashCode());
        result = prime * result + ((assetsRelativePathAndBinaryContent == null) ? 0 : assetsRelativePathAndBinaryContent.hashCode());
        result = prime * result + ((assetsRelativePathAndTextContent == null) ? 0 : assetsRelativePathAndTextContent.hashCode());
        return result;
    }

    public void setAssetsFolderPath(String assetsFolderPath) {
        this.assetsFolderPath = assetsFolderPath;
    }

    public void setAssetsRelativePathAndBinaryContent(List<Tuple2<String, byte[]>> assetsRelativePathAndBinaryContent) {
        this.assetsRelativePathAndBinaryContent = assetsRelativePathAndBinaryContent;
    }

    public void setAssetsRelativePathAndTextContent(List<Tuple2<String, String>> assetsRelativePathAndTextContent) {
        this.assetsRelativePathAndTextContent = assetsRelativePathAndTextContent;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IPApplicationDefinitionAssetsBundle [assetsFolderPath=");
        builder.append(assetsFolderPath);
        builder.append(", assetsRelativePathAndTextContent=");
        builder.append(assetsRelativePathAndTextContent);
        builder.append(", assetsRelativePathAndBinaryContent=");
        builder.append(assetsRelativePathAndBinaryContent);
        builder.append("]");
        return builder.toString();
    }

}
