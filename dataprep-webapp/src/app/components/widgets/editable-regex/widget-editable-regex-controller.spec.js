/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Editable regex widget controller', function () {
    'use strict';

    var scope;
    var createController;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            EQUALS: 'Equals',
            CONTAINS: 'Contains',
            STARTS_WITH: 'Starts With',
            ENDS_WITH: 'Ends With',
            REGEX: 'RegEx',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = function () {
            var ctrlFn = $controller('TalendEditableRegexCtrl', {
                $scope: scope,
            }, true);
            ctrlFn.instance.value = scope.value;
            return ctrlFn();
        };
    }));

    it('should init types array', function () {
        //when
        var ctrl = createController();

        //then
        expect(ctrl.types.length).toBe(5);
        expect(ctrl.types[0].label).toBe('Equals');
        expect(ctrl.types[1].label).toBe('Contains');
        expect(ctrl.types[2].label).toBe('Starts With');
        expect(ctrl.types[3].label).toBe('Ends With');
        expect(ctrl.types[4].label).toBe('RegEx');
    });

    it('should return the initialized operator key', function () {
        //when
        var ctrl = createController();

        //then
        expect(ctrl.value).toEqual({
            token: '',
            operator: 'contains',
        });
    });

    it('should return the current operator key', function () {
        scope.value = {
            token: '',
            operator: 'starts_with',
        };

        //when
        var ctrl = createController();
        var key = ctrl.getTypeKey();

        //then
        expect(key).toBe('>');
    });

    it('should update regex type', function () {
        scope.value = {
            token: '',
            operator: 'starts_with',
        };

        //when
        var ctrl = createController();
        ctrl.setSelectedType({ operator: 'regex' });

        //then
        expect(ctrl.value.operator).toBe('regex');
    });
});
