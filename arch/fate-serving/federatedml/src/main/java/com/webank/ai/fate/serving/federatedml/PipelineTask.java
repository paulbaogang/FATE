package com.webank.ai.fate.serving.federatedml;

import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.PipelineProto;
import com.webank.ai.fate.serving.federatedml.model.BaseModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PipelineTask {
    private List<BaseModel> pipeLineNode = new ArrayList<>();
    private String modelPackage = "com.webank.ai.fate.serving.federatedml.model";
    private static final Logger LOGGER = LogManager.getLogger();

    public int initModel(Map<String, byte[]> modelProtoMap) {
        LOGGER.info("start init Pipeline");
        try {
            for (String key : modelProtoMap.keySet()){
                LOGGER.info("key:{}, value:{}", key.toString(), modelProtoMap.get(key));
            }
            String pipelineProtoName = "Pipeline";
            LOGGER.info("Pipeline.getBytes:{}", pipelineProtoName.getBytes());
            PipelineProto.Pipeline pipeLineProto = PipelineProto.Pipeline.parseFrom(modelProtoMap.get(pipelineProtoName.getBytes()));
            List<String> pipeLineMeta = pipeLineProto.getNodeMetaList();
            List<String> pipeLineParam = pipeLineProto.getNodeParamList();
            LOGGER.info("end init Pipeline");
            for (int i = 0; i < pipeLineMeta.size(); i++) {
                try {
                    LOGGER.info("start get class name");
                    String className = pipeLineMeta.get(i).split(".")[0];
                    LOGGER.info("className:{}",className);
                    Class modelClass = Class.forName(this.modelPackage + "." + className);
                    BaseModel mlNode = (BaseModel) modelClass.getConstructor().newInstance();

                    byte[] protoMeta = modelProtoMap.get(pipeLineMeta.get(i));
                    byte[] protoParam = modelProtoMap.get(pipeLineParam.get(i));
                    mlNode.initModel(protoMeta, protoParam);

                    pipeLineNode.add(mlNode);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOGGER.info("Pipeline1 catch error:{}",ex);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.info("Pipeline2 catch error:{}",ex);
        }

        return StatusCode.OK;
    }

    public Map<String, Object> predict(Map<String, Object> inputData, Map<String, Object> predictParams) {
        for (int i = 0; i < this.pipeLineNode.size(); i++) {
            inputData = this.pipeLineNode.get(i).predict(inputData, predictParams);
        }

        return inputData;
    }
}
