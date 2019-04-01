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

package com.webank.ai.fate.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class SceneUtils {
    private static final String sceneKeySeparator = "_";

    public static String genSceneKey(String sceneId, String myRole, String myPartyId, String partnerPartyId){
        return StringUtils.join(Arrays.asList(sceneId, myRole, myPartyId, partnerPartyId), sceneKeySeparator);
    }

    public static String genSceneKey(int sceneId, String myRole, int myPartyId, int partnerPartyId){
        return StringUtils.join(Arrays.asList(sceneId, myRole, myPartyId, partnerPartyId), sceneKeySeparator);
    }
}
