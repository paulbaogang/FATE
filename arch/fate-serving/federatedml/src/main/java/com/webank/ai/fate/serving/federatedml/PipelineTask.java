/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.federatedml;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.core.network.grpc.client.ClientPool;
import com.webank.ai.fate.core.result.ReturnResult;
import com.webank.ai.fate.core.utils.Configuration;
import com.webank.ai.fate.core.utils.ObjectTransform;
import io.grpc.ManagedChannel;
import com.webank.ai.fate.api.networking.proxy.Proxy.Packet;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PipelineTask{
    private static final Logger LOGGER = LogManager.getLogger();

    public int initModel(Map<String, byte[]> modelProtoMap){
        return 0;
    }

    public Map<String, Object> predict(Map<String, Object> inputData, Map<String, Object> predictParams){
        Map<String, Object> result = new HashMap<>();
        result.put("r1", "xxx");
        Map<String, Object> hostResponse = getFederatedPredict(predictParams);
        LOGGER.info(hostResponse);
        return result;
    }


    protected Map<String, Object> getFederatedPredict(Map<String, Object> requestData){
        Packet.Builder packetBuilder = Packet.newBuilder();
        packetBuilder.setBody(Proxy.Data.newBuilder()
                .setValue(ByteString.copyFrom(ObjectTransform.bean2Json(requestData).getBytes()))
                .build());

        Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
        Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

        metaDataBuilder.setSrc(
                topicBuilder.setPartyId(Configuration.getProperty("partyId")).
                        setRole("guest")
                        .setName("partyName")
                        .build());
        metaDataBuilder.setDst(
                topicBuilder.setPartyId(requestData.get("partnerPartyId").toString())
                        .setRole("host")
                        .setName("partnerPartyName")
                        .build());
        metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName("federatedPredict").build());
        packetBuilder.setHeader(metaDataBuilder.build());

        ManagedChannel channel1 = ClientPool.getChannel(Configuration.getProperty("proxy"));
        DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
        Packet packet = stub1.unaryCall(packetBuilder.build());

        ReturnResult result = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
        return result.getData();
    }
}
