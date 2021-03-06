/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_DATASET_MODULE from '../../../../services/dataset/dataset-module';
import SERVICES_PLAYGROUND_MODULE from '../../../../services/playground/playground-module';
import SERVICES_UTILS_MODULE from '../../../../services/utils/utils-module';

import TypeTransformMenuCtrl from './type-transformation-menu-controller';
import TypeTransformMenu from './type-transformation-menu-directive';

const MODULE_NAME = 'data-prep.type-transformation-menu';

/**
 * @ngdoc object
 * @name data-prep.type-transformation-menu
 * @description This module contains the controller
 * and directives to manage the type transformation menu items
 * @requires data-prep.services.dataset
 * @requires data-prep.services.playground
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_DATASET_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('TypeTransformMenuCtrl', TypeTransformMenuCtrl)
    .directive('typeTransformMenu', TypeTransformMenu);

export default MODULE_NAME;
