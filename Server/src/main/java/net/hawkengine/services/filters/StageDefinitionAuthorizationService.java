/*
 * Copyright (C) 2016 R&D Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.hawkengine.services.filters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.hawkengine.core.utilities.deserializers.MaterialDefinitionAdapter;
import net.hawkengine.core.utilities.deserializers.TaskDefinitionAdapter;
import net.hawkengine.core.utilities.deserializers.WsContractDeserializer;
import net.hawkengine.model.MaterialDefinition;
import net.hawkengine.model.PipelineDefinition;
import net.hawkengine.model.StageDefinition;
import net.hawkengine.model.TaskDefinition;
import net.hawkengine.model.dto.WsContractDto;
import net.hawkengine.services.PipelineDefinitionService;
import net.hawkengine.services.StageDefinitionService;
import net.hawkengine.services.filters.interfaces.IAuthorizationService;
import net.hawkengine.services.interfaces.IPipelineDefinitionService;
import net.hawkengine.services.interfaces.IStageDefinitionService;

import java.util.ArrayList;
import java.util.List;

public class StageDefinitionAuthorizationService implements IAuthorizationService {
    private IPipelineDefinitionService pipelineDefinitionService;
    private IStageDefinitionService stageDefinitionService;
    private IAuthorizationService pipelineDefintionAuthorizationService;
    private Gson jsonConverter;

    public StageDefinitionAuthorizationService() {
        this.pipelineDefinitionService = new PipelineDefinitionService();
        this.stageDefinitionService = new StageDefinitionService();
        this.pipelineDefintionAuthorizationService = new PipelineDefinitionAuthorizationService();
        this.jsonConverter = new GsonBuilder()
                .registerTypeAdapter(WsContractDto.class, new WsContractDeserializer())
                .registerTypeAdapter(TaskDefinition.class, new TaskDefinitionAdapter())
                .registerTypeAdapter(MaterialDefinition.class, new MaterialDefinitionAdapter())
                .create();
    }

    public StageDefinitionAuthorizationService(IPipelineDefinitionService pipelineDefinitionService, IStageDefinitionService stageDefinitionService, IAuthorizationService pipelineDefintionAuthorizationService) {
        this.pipelineDefinitionService = pipelineDefinitionService;
        this.stageDefinitionService = stageDefinitionService;
        this.pipelineDefintionAuthorizationService = pipelineDefintionAuthorizationService;
        this.jsonConverter = new GsonBuilder()
                .registerTypeAdapter(WsContractDto.class, new WsContractDeserializer())
                .registerTypeAdapter(TaskDefinition.class, new TaskDefinitionAdapter())
                .registerTypeAdapter(MaterialDefinition.class, new MaterialDefinitionAdapter())
                .create();
    }

    @Override
    public List getAll(List permissions, List entriesToFilter) {
        List<StageDefinition> filteredStageDefinitions = new ArrayList<>();
        List<PipelineDefinition> pipelineDefinitions = (List<PipelineDefinition>) this.pipelineDefinitionService.getAll().getObject();
        List<PipelineDefinition> filteredPipelineDefinitions = this.pipelineDefintionAuthorizationService.getAll(permissions, pipelineDefinitions);

        for (PipelineDefinition filteredPipelineDefinition : filteredPipelineDefinitions) {
            filteredStageDefinitions.addAll(filteredPipelineDefinition.getStageDefinitions());
        }
        return filteredStageDefinitions;
    }

    @Override
    public boolean getById(String entityId, List permissions) {
        StageDefinition stageDefinition = (StageDefinition) this.stageDefinitionService.getById(entityId).getObject();

        return this.pipelineDefintionAuthorizationService.getById(stageDefinition.getPipelineDefinitionId(), permissions);
    }

    @Override
    public boolean add(String entity, List permissions) {
        StageDefinition stageDefinition = this.jsonConverter.fromJson(entity, StageDefinition.class);
        PipelineDefinition pipelineDefinition = (PipelineDefinition) this.pipelineDefinitionService.getById(stageDefinition.getPipelineDefinitionId()).getObject();

        String pipelineDefinitionsAsString = this.jsonConverter.toJson(pipelineDefinition);

        return this.pipelineDefintionAuthorizationService.update(pipelineDefinitionsAsString, permissions);
    }

    @Override
    public boolean update(String entity, List permissions) {
        StageDefinition stageDefinition = this.jsonConverter.fromJson(entity, StageDefinition.class);
        PipelineDefinition pipelineDefinition = (PipelineDefinition) this.pipelineDefinitionService.getById(stageDefinition.getPipelineDefinitionId()).getObject();

        String pipelineDefinitionsAsString = this.jsonConverter.toJson(pipelineDefinition);

        return this.pipelineDefintionAuthorizationService.update(pipelineDefinitionsAsString, permissions);
    }

    @Override
    public boolean delete(String entityId, List permissions) {
        StageDefinition stageDefinition = (StageDefinition) this.stageDefinitionService.getById(entityId).getObject();

        return this.pipelineDefintionAuthorizationService.delete(stageDefinition.getPipelineDefinitionId(), permissions);
    }
}
