/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationService
 * @description Transformation service.
 * This service provide the entry point to get and manipulate transformations
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.transformation.service:TransformationRestService
 */
export default function TransformationService(TransformationRestService, ConverterService) {
    'ngInject';

    const choiceType = 'CHOICE';
    const clusterType = 'CLUSTER';
    const COLUMN_CATEGORY = 'column_metadata';

    return {
        getLineTransformations: getLineTransformations,
        getColumnTransformations: getColumnTransformations,
        getColumnSuggestions: getColumnSuggestions,
        resetParamValue: resetParamValue,
        initParamsValues: initParamsValues,
        initDynamicParameters: initDynamicParameters
    };


    /**
     * @ngdoc method
     * @name isExplicitParameter
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {object} param the parameter to check
     * @description Return true if the parameter is explicit based on the 'implicit' flag
     */
    function isExplicitParameter(param) {
        return !param.implicit;
    }

    // --------------------------------------------------------------------------------------------
    // ----------------------------------Transformations suggestions Utils-------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name cleanParams
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {object[]} menus - the menus to clean
     * @description Remove 'column_id' and 'column_name' parameters (automatically sent),
     * and clean empty arrays (choices and params)
     */
    function cleanParams(menus) {
        return _.forEach(menus, (menu) => {
            const filteredParameters = _.filter(menu.parameters, isExplicitParameter);
            menu.parameters = filteredParameters.length ? filteredParameters : null;
        });
    }

    /**
     * @ngdoc method
     * @name insertType
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {object[]} transformation The transformation with parameters to adapt
     * @description Insert adapted html input type in each parameter in the menu
     */
    function insertType(transformation) {
        if (transformation.parameters) {
            _.forEach(transformation.parameters, (param) => {
                param.inputType = ConverterService.toInputType(param.type);

                // also take care of select parameters...
                if (param.type === 'select' && param.configuration && param.configuration.values) {
                    _.forEach(param.configuration.values, (selectItem) => {
                        selectItem.inputType = ConverterService.toInputType(selectItem.type);
                        // ...and its parameters
                        if (selectItem.parameters) {
                            insertType(selectItem);
                        }
                    });
                }
            });
        }
    }

    /**
     * @ngdoc method
     * @name insertInputTypes
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {Array} transformations The transformations with parameters to adapt
     * @description Insert parameter type to HTML input type in each transformations
     */
    function insertInputTypes(transformations) {
        _.forEach(transformations, insertType);
    }

    /**
     * @ngdoc method
     * @name setHtmlDisplayLabels
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @description Inject the UI label in each transformations
     * @param {Array} transformations The list of transformations
     */
    function setHtmlDisplayLabels(transformations) {
        _.forEach(transformations, (transfo) => {
            transfo.labelHtml =
                transfo.label + (transfo.parameters || transfo.dynamic ? '...' : '');
        });
    }

    function isNotColumnCategory(category) {
        return (item) => {
            return item.category !== category;
        };
    }

    function labelCriteria(transfo) {
        return transfo.label.toLowerCase();
    }

    /**
     * @ngdoc method
     * @name prepareTransformations
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @description Sort and group transformations by category
     * @return {Object} An object {category, categoryHtml, transformations} .
     * "category" the category
     * "categoryHtml" the adapted category for UI
     * "transformations" the array of transformations for this category
     */
    function prepareTransformations(transformations) {
        const groupedTransformations = _.chain(transformations)
            .filter(isNotColumnCategory(COLUMN_CATEGORY))
            .sortBy(labelCriteria)
            .groupBy('category')
            .value();

        return _.chain(Object.getOwnPropertyNames(groupedTransformations))
            .sort()
            .map((key) => {
                return {
                    category: key,
                    categoryHtml: key.toUpperCase(),
                    transformations: groupedTransformations[key]
                };
            })
            .value();
    }

    // --------------------------------------------------------------------------------------------
    // -----------------------------------Transformations suggestions------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getLineTransformations
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @description Get transformations from REST call, clean and adapt them
     * @return {Object} An object {allTransformations, allCategories} .
     * "allTransformations" is the array of all transformations (cleaned and adapted for UI)
     * "allCategories" is the array of all transformations grouped by category
     */
    function getLineTransformations() {
        return TransformationRestService.getLineTransformations()
            .then((response) => {
                const allTransformations = cleanParams(response.data);
                insertInputTypes(allTransformations);
                setHtmlDisplayLabels(allTransformations);
                const allCategories = prepareTransformations(allTransformations);
                return {
                    allTransformations: allTransformations,
                    allCategories: allCategories
                };
            });
    }

    /**
     * @ngdoc method
     * @name getColumnTransformations
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {object} column The transformations target column
     * @description Get transformations from REST call, clean and adapt them
     * @return {Object} An object {allTransformations, allCategories} .
     * "allTransformations" is the array of all transformations (cleaned and adapted for UI)
     * "allCategories" is the array of all transformations grouped by category
     */
    function getColumnTransformations(column) {
        return TransformationRestService.getColumnTransformations(column)
            .then((response) => {
                const allTransformations = cleanParams(response.data);
                insertInputTypes(allTransformations);
                setHtmlDisplayLabels(allTransformations);
                const allCategories = prepareTransformations(allTransformations);
                return {
                    allTransformations: allTransformations,
                    allCategories: allCategories
                };
            });
    }

    /**
     * @ngdoc method
     * @name getColumnSuggestions
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {object} column The transformations target column
     * @description Get suggestions from REST call, clean and adapt them
     * @returns {Array} All the suggestions, cleaned and adapted for UI
     */
    function getColumnSuggestions(column) {
        return TransformationRestService.getColumnSuggestions(column)
            .then((response) => {
                const allTransformations = cleanParams(response.data);
                insertInputTypes(allTransformations);
                setHtmlDisplayLabels(allTransformations);
                return allTransformations;
            });
    }

    // --------------------------------------------------------------------------------------------
    // -----------------------------------Transformation parameters--------------------------------
    // --------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name resetParamValue
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @param {object} params The params to reset
     * @param {string} type The param type
     * @description [PRIVATE] Reset params values with saved initial values
     */
    function resetParamValue(params, type) {
        if (!params) {
            return;
        }

        function executeOnSimpleParams(simpleParamsToInit) {
            _.forEach(simpleParamsToInit, (param) => {
                param.value = angular.isDefined(param.initialValue) ?
                    param.initialValue :
                    param.default;
            });
        }

        switch (type) {
            case choiceType:
                _.forEach(params, (choice) => {
                    choice.selectedValue = angular.isDefined(choice.initialValue) ?
                        choice.initialValue :
                        choice.default;

                    _.forEach(choice.values, (choiceItem) => {
                        executeOnSimpleParams(choiceItem.parameters);
                    });
                });
                break;

            case clusterType:
                _.forEach(params.clusters, (cluster) => {
                    cluster.active = cluster.initialActive;
                    executeOnSimpleParams(cluster.parameters);
                    executeOnSimpleParams([cluster.replace]);
                });
                break;

            default:
                executeOnSimpleParams(params);
        }
    }

    /**
     * @ngdoc method
     * @name initParameters
     * @methodOf data-prep.services.recipe.service:RecipeService
     * @param {object} parameters The parameters
     * @param {object} paramValues The parameters initial values
     * @description Init parameters initial value and type
     * @returns {object[]} The parameters with initialized values
     */
    function initParameters(parameters, paramValues) {
        return _.chain(parameters)
            .filter(isExplicitParameter)
            .forEach((param) => {
                param.initialValue = param.value = ConverterService.adaptValue(
                    param.type,
                    paramValues[param.name]
                );
                param.inputType = ConverterService.toInputType(param.type);

                // also take care of select parameters
                if (param.type === 'select' && param.configuration && param.configuration.values) {
                    _.forEach(param.configuration.values, (selectItem) => {
                        initParameters(selectItem.parameters, paramValues);
                    });
                }
            })
            .value();
    }


    /**
     * @ngdoc method
     * @name initCluster
     * @methodOf data-prep.services.recipe.service:RecipeService
     * @param {object} cluster The Cluster parameters
     * @param {object} paramValues The clusters initial values
     * @description Init Clusters initial value
     * @returns {object} The Cluster with initialized values
     */
    function initCluster(cluster, paramValues) {
        _.forEach(cluster.clusters, (clusterItem) => {
            const firstActiveParam = _.chain(clusterItem.parameters)
                .forEach((param) => {
                    param.initialValue = param.value = param.name in paramValues;
                })
                .filter('value')
                .first()
                .value();
            clusterItem.initialActive = !!firstActiveParam;

            //get the replace value or the default if the cluster item is inactive
            //and init the replace input value
            const replaceValue = firstActiveParam ?
                paramValues[firstActiveParam.name] :
                clusterItem.replace.default;
            const replaceParamValues = { replaceValue: replaceValue };
            initParameters([clusterItem.replace], replaceParamValues);
        });
        return cluster;
    }

    /**
     * @ngdoc method
     * @name initParamsValues
     * @methodOf data-prep.services.recipe.service:RecipeService
     * @param {object} transformation The transformation infos
     * @param {object} paramValues The transformation parameters initial values
     * @description Init parameters values and save them as initial values
     */
    function initParamsValues(transformation, paramValues) {
        if (transformation.parameters) {
            transformation.parameters = initParameters(transformation.parameters, paramValues);
        }
        if (transformation.cluster) {
            transformation.cluster = initCluster(transformation.cluster, paramValues);
        }
    }

    // --------------------------------------------------------------------------------------------
    // ---------------------------Transformation Dynamic parameters--------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name resetParameters
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @description Reset all the transformation parameters
     */
    function resetParameters(transformation) {
        transformation.parameters = null;
        transformation.cluster = null;
    }

    /**
     * @ngdoc method
     * @name initDynamicParameters
     * @methodOf data-prep.services.transformation.service:TransformationService
     * @description Fetch the dynamic parameter and set them in transformation
     */
    function initDynamicParameters(transformation, infos) {
        resetParameters(transformation);

        const action = transformation.name;
        return TransformationRestService
            .getDynamicParameters(
                action,
                infos.columnId,
                infos.datasetId,
                infos.preparationId,
                infos.stepId
            )
            .then((response) => {
                const parameters = response.data;
                transformation[parameters.type] = parameters.details;
                return transformation;
            });
    }
}
