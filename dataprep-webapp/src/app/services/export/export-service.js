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
 * @name data-prep.services.export.service:ExportService
 * @description Export service. This service provide the entry point to the backend export REST api.
 * @requires data-prep.services.export.service:ExportRestService
 * @requires data-prep.services.transformations.service:TransformationService
 * @requires data-prep.services.utils.service:StorageService
 */
export default class ExportService {
    constructor(StorageService, ExportRestService, TransformationService) {
        'ngInject';

        this.StorageService = StorageService;
        this.ExportRestService = ExportRestService;
        this.TransformationService = TransformationService;

        this.exportTypes = [];
    }

    /**
     * @ngdoc method
     * @name reset
     * @methodOf data-prep.services.export.service:ExportService
     * @description Reset the export types parameters
     */
    reset() {
        _.forEach(this.exportTypes, (type) => {
            this.TransformationService.resetParamValue(type.parameters);
        });
    }


    /**
     * @ngdoc method
     * @name getType
     * @methodOf data-prep.services.export.service:ExportService
     * @description Get the type by id
     */
    getType(id) {
        return _.find(this.exportTypes, { id: id });
    }

    /**
     * @ngdoc method
     * @name saveDefaultExport
     * @methodOf data-prep.services.export.service:ExportService
     * @description Save the default export in localStorage
     */
    _saveDefaultExport() {
        const exportType = _.find(this.exportTypes, { defaultExport: 'true' });
        this.StorageService.saveExportParams({ exportType: exportType.id });
    }

    /**
     * @ngdoc method
     * @name refreshTypes
     * @methodOf data-prep.services.export.service:ExportService
     * @description Refresh the export types list and save default if no parameters has been saved yet
     */
    refreshTypes() {
        return this.ExportRestService.exportTypes()
            .then((exportTypes) => {
                this.exportTypes = exportTypes;
            })
            .then(() => {
                // save default export if no parameter has been saved yet
                if (!this.StorageService.getExportParams() && this.exportTypes.length) {
                    this._saveDefaultExport();
                }
            });
    }
}
