/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './recipe.html';

/**
 * @ngdoc directive
 * @name data-prep.recipe.directive:Recipe
 * @description This directive display the recipe with the step params as accordions.
 * @restrict E
 * @usage <recipe></recipe>
 */
export default function Recipe($timeout) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: template,
        controllerAs: 'recipeCtrl',
        controller: 'RecipeCtrl',
        link(scope, iElement, iAttrs, ctrl) {
            function attachDeleteMouseOver(allSteps) {
                _.forEach(allSteps, function (step) {
                    const stepId = step.transformation.stepId;
                    const hasDiff = step.diff && step.diff.createdColumns && step.diff.createdColumns.length;

                    function shouldBeRemoved(stepToTest) {
                        return stepToTest.transformation.stepId === stepId || // current step
                            (hasDiff && stepToTest.actionParameters &&
                            step.diff.createdColumns.indexOf(stepToTest.actionParameters.parameters.column_id) > -1); // step on a column that will be removed
                    }

                    const stepsToRemove = _.chain(allSteps)
                        .filter(shouldBeRemoved)
                        .map(function (step) {
                            return iElement.find('#step-' + step.transformation.stepId);
                        })
                        .value();

                    const removeElement = iElement.find('#step-' + stepId).find('#step-remove-' + stepId);
                    removeElement.on('mouseover', function () {
                        _.forEach(stepsToRemove, function (stepElement) {
                            stepElement.addClass('remove');
                        });
                    });

                    removeElement.on('mouseout', function () {
                        _.forEach(stepsToRemove, function (stepElement) {
                            stepElement.removeClass('remove');
                        });
                    });
                });
            }

            scope.$watch(
                () => ctrl.state.playground.recipe.current.steps,
                (allSteps) => {
                    $timeout(attachDeleteMouseOver.bind(null, allSteps), 0, false);
                }
            );
        },
    };
}
