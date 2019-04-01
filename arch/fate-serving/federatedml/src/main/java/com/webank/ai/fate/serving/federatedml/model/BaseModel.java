package com.webank.ai.fate.serving.federatedml.model;

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.core.network.grpc.client.ClientPool;
import com.webank.ai.fate.core.result.ReturnResult;
import com.webank.ai.fate.core.utils.Configuration;
import com.webank.ai.fate.core.utils.ObjectTransform;
import io.grpc.ManagedChannel;

import java.util.Map;

public abstract class BaseModel {
    public abstract int initModel(byte[] protoMeta, byte[] protoParam);

    // public abstract HashMap<String, Object> predict(HashMap<String, Object> inputData);
    public abstract Map<String, Object> predict(Map<String, Object> inputData, Map<String, Object> predictParams);

    private Map<String, String> modelInfo;

    public void setModelInfo(Map<String, String> modelInfo) {
        this.modelInfo = modelInfo;
    }

    public Map<String, String> getModelInfo() {
        return this.modelInfo;
    }

    protected Map<String, Object> getFederatedPredict(Map<String, Object> requestData) {
        Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
        requestData.putAll(this.modelInfo);
        requestData.put("myPartyId", Configuration.getProperty("partyId"));
        requestData.put("modelName", requestData.get("partnerModelName"));
        requestData.put("modelNamespace", requestData.get("partnerModelNamespace"));
        packetBuilder.setBody(Proxy.Data.newBuilder()
                .setValue(ByteString.copyFrom(ObjectTransform.bean2Json(requestData).getBytes()))
                .build());

        Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
        Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

        metaDataBuilder.setSrc(
                topicBuilder.setPartyId(Configuration.getProperty("partyId")).
                        setRole(this.modelInfo.get("myRole"))
                        .setName("partyName")
                        .build());
        metaDataBuilder.setDst(
                topicBuilder.setPartyId(this.modelInfo.get("partnerPartyId"))
                        .setRole("host")
                        .setName("partnerPartyName")
                        .build());
        metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName("federatedPredict").build());
        packetBuilder.setHeader(metaDataBuilder.build());

        ManagedChannel channel1 = ClientPool.getChannel(Configuration.getProperty("proxy"));
        DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
        Proxy.Packet packet = stub1.unaryCall(packetBuilder.build());

        ReturnResult result = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
        return result.getData();
    }
}
