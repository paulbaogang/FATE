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

import com.webank.ai.fate.core.mlmodel.model.MLModel;
import com.webank.ai.fate.core.result.ReturnResult;
import com.webank.ai.fate.core.constant.StatusCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.Map;

public class ModelManager {
    private static ModelPool sceneModel;
    private static ModelCache modelCache;
    private static final Logger LOGGER = LogManager.getLogger();
    public ModelManager(){
        sceneModel = new ModelPool();
        modelCache = new ModelCache();
    }

    public static MLModel getModel(String sceneId, String partnerPartyId, String myRole){
        return sceneModel.get(ModelUtils.getSceneModelKey(sceneId, partnerPartyId, myRole));
    }

    public static MLModel getModel(String name, String namespace){
        return modelCache.get(ModelUtils.genModelKey(name, namespace));
    }

    private static int pushModelIntoPool(String name, String namespace) throws Exception{
        MLModel mlModel = ModelUtils.loadModel(name, namespace);
        if (mlModel == null){
            return StatusCode.NOMODEL;
        }
        modelCache.put(ModelUtils.genModelKey(name, namespace), mlModel);
        return StatusCode.OK;
    }

    public static ReturnResult publishLoadModel(String name, String namespace){
        ReturnResult returnResult = new ReturnResult();
        returnResult.setStatusCode(StatusCode.OK);
        returnResult.setData("name", name);
        returnResult.setData("namespace", namespace);
        try{
            int localLoadStatus = pushModelIntoPool(name, namespace);
            if (localLoadStatus != StatusCode.OK){
                returnResult.setStatusCode(localLoadStatus);
                return returnResult;
            }
        }
        catch (IOException ex){
            LOGGER.error(ex);
            returnResult.setStatusCode(StatusCode.IOERROR);
            returnResult.setError(ex.getMessage());
        }
        catch (Exception ex){
            LOGGER.error(ex);
            returnResult.setStatusCode(StatusCode.UNKNOWNERROR);
            returnResult.setError(ex.getMessage());
        }
        return returnResult;
    }

    public static ReturnResult federatedLoadModel(Map<String, Object> requestData){
        ReturnResult returnResult = new ReturnResult();
        returnResult.setStatusCode(StatusCode.OK);
        try{
            String name = String.valueOf(requestData.get("modelName"));
            String namespace = String.valueOf(requestData.get("modelNamespace"));
            returnResult.setData("name", name);
            returnResult.setData("namespace", namespace);
            returnResult.setStatusCode(pushModelIntoPool(name, namespace));
        }
        catch (Exception ex){
            returnResult.setStatusCode(StatusCode.UNKNOWNERROR);
            returnResult.setMessage(ex.getMessage());
        }
        return returnResult;
    }

    public static ReturnResult publishOnlineModel(String sceneId, String myRole, String partnerPartyId, String name, String namespace){
        ReturnResult returnResult = new ReturnResult();
        MLModel model = modelCache.get(ModelUtils.genModelKey(name, namespace));
        if (model == null){
            returnResult.setStatusCode(StatusCode.NOMODEL);
            returnResult.setMessage("Can not found model by these information.");
            return returnResult;
        }
        try{
            sceneModel.put(ModelUtils.getSceneModelKey(sceneId, myRole, partnerPartyId), model);
            returnResult.setStatusCode(StatusCode.OK);
        }
        catch (Exception ex){
            returnResult.setStatusCode(StatusCode.UNKNOWNERROR);
            returnResult.setMessage(ex.getMessage());
        }
        return returnResult;
    }
}
