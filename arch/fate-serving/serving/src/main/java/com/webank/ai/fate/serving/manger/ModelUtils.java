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

package com.webank.ai.fate.serving.manger;

import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.ProtoModelBuffer;
import com.webank.ai.fate.core.mlmodel.model.MLModel;
import com.webank.ai.fate.core.storage.dtable.DTable;
import com.webank.ai.fate.core.storage.dtable.DTableFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModelUtils {
    private static String modelPackage = "com.webank.ai.fate.serving.federatedml.model";

    public static ProtoModelBuffer readModel(String name, String namespace){
        //DTable dataTable = VersionControl.dTableForRead("model_data", sceneId, partnerPartyId, myRole, commitId, tag, branch);
        DTable dataTable = DTableFactory.getDTable(name, namespace, 1);
        ProtoModelBuffer modelBuffer = new ProtoModelBuffer();
        if (modelBuffer.deserialize(dataTable.get("model_meta"), dataTable.get("model_param"), dataTable.get("data_transform")) == StatusCode.OK){
            return modelBuffer;
        }
        else{
            return null;
        }
    }


    public static MLModel loadModel(String name, String namespace) throws Exception{
        ProtoModelBuffer modelBuffer = readModel(name, namespace);
        if (modelBuffer == null){
            return null;
        }
        Class modelClass = Class.forName(modelPackage + "." + modelBuffer.getMeta().getName());
        MLModel mlModel = (MLModel)modelClass.getConstructor().newInstance();
        Map<String, String> modelInfo = new HashMap<>();
        mlModel.setModelInfo(modelInfo);
        mlModel.initModel(modelBuffer);
        return mlModel;
    }

    public static String getSceneModelKey(String sceneId, String myRole, String partnerPartyId){
        return StringUtils.join(Arrays.asList(sceneId, myRole, partnerPartyId), "-");
    }

    public static String genModelKey(String name, String namespace){
        return StringUtils.join(Arrays.asList(name, namespace), "-");
    }

    public static String[] splitModelKey(String key){
        return StringUtils.split(key, "-");
    }
}
