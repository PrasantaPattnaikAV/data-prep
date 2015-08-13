(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.playground.service:DatagridService
     * @description Datagrid service. This service holds the datagrid (SlickGrid) view and the (SlickGrid) filters<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly for FILTERS.
     * {@link data-prep.services.filter.service:FilterService FilterService} must be the only entry point for datagrid filters</b>
     */
    function DatagridService(ConverterService) {
        var self = this;

        /**
         * @ngdoc property
         * @name metadata
         * @propertyOf data-prep.services.playground.service:DatagridService
         * @description the loaded metadata
         * @type {Object}
         */
        self.metadata = null;

        /**
         * @ngdoc property
         * @name data
         * @propertyOf data-prep.services.playground.service:DatagridService
         * @description the loaded data
         * @type {Object}
         */
        self.data = null;

        /**
         * @ngdoc property
         * @name dataView
         * @propertyOf data-prep.services.playground.service:DatagridService
         * @description the SlickGrid dataView
         * @type {Object}
         */
        self.dataView = new Slick.Data.DataView({inlineFilters: false});

        /**
         * @ngdoc property
         * @name filters
         * @propertyOf data-prep.services.playground.service:DatagridService
         * @description the filters applied to the dataview
         * @type {function[]}
         */
        self.filters = [];

        //------------------------------------------------------------------------------------------------------
        //---------------------------------------------------DATA-----------------------------------------------
        //------------------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name updateDataviewRecords
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {Object[]} records - the records to insert
         * @description [PRIVATE] Set dataview records
         */
        var updateDataviewRecords = function (records) {
            self.dataView.beginUpdate();
            self.dataView.setItems(records, 'tdpId');
            self.dataView.endUpdate();
        };

        /**
         * @ngdoc method
         * @name setDataset
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {Object} metadata - the new metadata to load
         * @param {Object} data - the new data to load
         * @description Set dataview records and metadata to the datagrid
         */
        self.setDataset = function (metadata, data) {
            updateDataviewRecords(data.records);
            self.metadata = metadata;
            self.data = data;
        };

        /**
         * @ngdoc method
         * @name setFocusedColumn
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {string} focusedColumn - the id of the recently focused column
         * @description updates the focusedColumn id
         */
        self.setFocusedColumn = function (focusedColumn){
            self.focusedColumn = focusedColumn;
        };

        function getLastNewColumnId(columns){
            var ancientColumnsIds = _.pluck(self.data.columns, 'id');
            var newColumnsIds = _.pluck(columns, 'id');
            var diffIds = _.difference(newColumnsIds, ancientColumnsIds);

            return diffIds[diffIds.length - 1];
        }

        /**
         * @ngdoc method
         * @name updateData
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {Object} data - the new data (columns and records)
         * @description Update the data in the datagrid
         */
        self.updateData = function (data) {
            if(self.data.columns.length < data.columns.length){
                var lastNewColId = getLastNewColumnId(data.columns);
                self.setFocusedColumn(lastNewColId);
            }
            self.data = data;
            updateDataviewRecords(data.records);
        };

        //------------------------------------------------------------------------------------------------------
        //------------------------------------------------DATA UTILS--------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getColumns
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {boolean} excludeNumeric - filter the numeric columns
         * @param {boolean} excludeBoolean - filter the boolean columns
         * @description Filter the column ids
         * @returns {Object[]} - the column list that match the desired filters (id & name)
         */
        self.getColumns = function(excludeNumeric, excludeBoolean) {
            var numericTypes = ['numeric', 'integer', 'float', 'double'];
            var cols = self.data.columns;

            if(excludeNumeric) {
                cols = _.filter(cols, function (col) {
                    return numericTypes.indexOf(col.type) === -1;
                });
            }
            if(excludeBoolean) {
                cols = _.filter(cols, function(col) {
                    return col.type !== 'boolean';
                });
            }

            return _.map(cols, function (col) {
                return {'id': col.id, 'name': col.name};
            });
        };


        /**
         * @ngdoc method
         * @name getNumberColumns
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {string} id - column to be excluded
         * @description Filter the column ids
         * @returns {Object[]} - the number columns list that match the desired filters (id & name)
         */
        self.getNumberColumns = function(id) {
            var cols = self.data.columns;

            cols = _.filter(cols, function (col) {
                if(id){
                    return (ConverterService.simplifyType(col.type) === 'number') && (col.id !== id);
                }
                return (ConverterService.simplifyType(col.type) === 'number');
            });

            return cols;
        };

        /**
         * @ngdoc method
         * @name getColumnsContaining
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {string} regexp - the regexp
         * @param {boolean} canBeNumeric - filter the numeric columns
         * @param {boolean} canBeBoolean - filter the boolean columns
         * @description Return the column id list that has a value that match the regexp
         * @returns {Object[]} - the column list that contains a value that match the regexp (col.id & col.name)
         */
        self.getColumnsContaining = function(regexp, canBeNumeric, canBeBoolean) {
            var results = [];

            var data = self.data.records;
            var potentialColumns = self.getColumns(!canBeNumeric, !canBeBoolean);

            //we loop over data while there is data and potential columns that can contains the searched term
            //if a col value for a row contains the term, we add it to result
            var dataIndex = 0;
            while (dataIndex < data.length && potentialColumns.length) {
                var record = data[dataIndex];
                for (var colIndex in potentialColumns) {
                    var col = potentialColumns[colIndex];
                    if (record[col.id].toLowerCase().match(regexp)) {
                        potentialColumns.splice(colIndex, 1);
                        results.push(col);
                    }
                }

                potentialColumns = _.difference(potentialColumns, results);
                dataIndex++;
            }

            return results;
        };

        /**
         * @ngdoc method
         * @name getSameContentConfig
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {string} colId The column index
         * @param {string} term The cell content to search
         * @param {string} cssClass The css class to apply
         * @description Return displayed rows index where data[rowId][colId] contains the searched term
         * @returns {Object} The SlickGrid css config for each column with the provided content
         */
        self.getSameContentConfig = function(colId, term, cssClass) {
            var config = {};
            for(var i = 0; i < self.dataView.getLength(); ++i) {
                var item = self.dataView.getItem(i);
                if(term === item[colId]) {
                    config[i] = {};
                    config[i][colId] = cssClass;
                }
            }

            return config;
        };

        //------------------------------------------------------------------------------------------------------
        //-------------------------------------------------FILTERS----------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name filterFn
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {object} item - the item to test
         * @param {object} args - object containing the filters predicates
         * @description [PRIVATE] Filter function. It iterates over all filters and return if the provided item fit the predicates
         * @returns {boolean} - true if the item pass all the filters
         */
        function filterFn(item, args) {
            for (var i = 0; i < args.filters.length; i++) {
                var filter = args.filters[i];
                if(!filter(item)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @ngdoc method
         * @name getAllFiltersFn
         * @methodOf data-prep.services.playground.service:DatagridService
         * @description [PRIVATE] Create a closure that contains the active filters to execute
         * @returns {function} The filters closure
         */
        self.getAllFiltersFn = function() {
            return function(item) {
                return filterFn(item, self);
            };
        };

        /**
         * @ngdoc method
         * @name updateDataViewFilters
         * @methodOf data-prep.services.playground.service:DatagridService
         * @description [PRIVATE] Update filters in dataview
         */
        var updateDataViewFilters = function() {
            self.dataView.beginUpdate();
            self.dataView.setFilterArgs({
                filters: self.filters
            });
            self.dataView.setFilter(filterFn);
            self.dataView.endUpdate();
        };

        /**
         * @ngdoc method
         * @name addFilter
         * @methodOf data-prep.services.playground.service:DatagridService
         * @description Add a filter in dataview
         * @param {object} filter - the filter function to add
         */
        self.addFilter = function(filter) {
            self.filters.push(filter);
            updateDataViewFilters();
        };

        /**
         * @ngdoc method
         * @name updateFilter
         * @methodOf data-prep.services.playground.service:DatagridService
         * @description Update a filter in dataview
         * @param {object} oldFilter - the filter function to replace
         * @param {object} newFilter - the new filter function
         */
        self.updateFilter = function(oldFilter, newFilter) {
            var index = self.filters.indexOf(oldFilter);
            self.filters.splice(index, 1, newFilter);
            updateDataViewFilters();
        };

        /**
         * @ngdoc method
         * @name removeFilter
         * @methodOf data-prep.services.playground.service:DatagridService
         * @description Remove a filter in dataview
         * @param {object} filter - the filter function to remove
         */
        self.removeFilter = function(filter) {
            var filterIndex = self.filters.indexOf(filter);
            if(filterIndex > -1) {
                self.filters.splice(filterIndex, 1);
                updateDataViewFilters();
            }
        };

        /**
         * @ngdoc method
         * @name resetFilters
         * @methodOf data-prep.services.playground.service:DatagridService
         * @description Remove all filters from dataview
         */
        self.resetFilters = function() {
            self.filters = [];
            updateDataViewFilters();
        };
    }

    angular.module('data-prep.services.playground')
        .service('DatagridService', DatagridService);

})();