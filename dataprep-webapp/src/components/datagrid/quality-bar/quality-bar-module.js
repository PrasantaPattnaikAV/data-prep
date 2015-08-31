(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.quality-bar
     * @description This module contains the controller and directives to manage the datagrid quality-bar
     * @requires data-prep.services.filter
     * @requires data-prep.services.transformation.service:ColumnSuggestionService
     * @requires data-prep.services.transformationApplication.service:TransformationApplicationService
     */
    angular.module('data-prep.quality-bar', [
        'data-prep.services.filter',
        'data-prep.services.transformationApplication'
    ]);
})();