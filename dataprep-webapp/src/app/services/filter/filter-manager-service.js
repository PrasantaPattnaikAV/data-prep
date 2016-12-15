/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';

const RANGE_SEPARATOR = ' .. ';

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterService
 * @description Filter service. This service provide the entry point to datagrid filters
 * @requires data-prep.services.filter.service:FilterAdapterService
 * @requires data-prep.services.statistics.service:StatisticsService
 */
export default class FilterManagerService {
	constructor($timeout, state, FilterAdapterService, StatisticsService,
		StorageService, FilterService) {
		'ngInject';

		this.$timeout = $timeout;
		this.state = state;
		this.FilterAdapterService = FilterAdapterService;
		this.StatisticsService = StatisticsService;
		this.StorageService = StorageService;
		this.FilterService = FilterService;
		this.CTRL_KEY_NAME = FilterService.CTRL_KEY_NAME;
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------UTILS------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name getRangeLabelFor
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Define an interval label
	 * @param {Object} interval
	 * @param {boolean} isDateRange
	 * @return {string} interval label
	 */
	getRangeLabelFor(interval, isDateRange) {
		let label;
		const formatDate = d3.time.format('%Y-%m-%d');
		const formatNumber = d3.format(',');
		let min;
		let max;
		if (isDateRange) {
			min = formatDate(new Date(interval.min));
			max = formatDate(new Date(interval.max));
		}
		else if (angular.isNumber(interval.min)) {
			min = formatNumber(interval.min);
			max = formatNumber(interval.max);
		}
		else {
			min = interval.min;
			max = interval.max;
		}

		if (min === max) {
			label = '[' + min + ']';
		}
		else {
			label = '[' + min + RANGE_SEPARATOR + max + (interval.isMaxReached ? ']' : '[');
		}

		return label;
	}

	/**
	 * @ngdoc method
	 * @name addFilterAndDigest
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} type The filter type (ex : contains)
	 * @param {string} colId The column id
	 * @param {string} colName The column name
	 * @param {string} args The filter arguments (ex for 'contains' type : {phrase: 'toto'})
	 * @param {function} removeFilterFn An optional remove callback
	 * @description Wrapper on addFilter method that trigger a digest at the end (use of $timeout)
	 */
	addFilterAndDigest(type, colId, colName, args, removeFilterFn, keyName) {
		this.$timeout(this.addFilter.bind(this, type, colId, colName, args, removeFilterFn, keyName));
	}

	/**
	 * @ngdoc method
	 * @name removeAllFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Remove all the filters and update datagrid filters
	 */
	removeAllFilters() {
		this.FilterService.removeAllFilters();
		this.StatisticsService.updateFilteredStatistics();
		this.StorageService.removeFilter(this.state.playground.preparation ? this.state.playground.preparation.id : this.state.playground.dataset.id);
	}

	/**
	 * @ngdoc method
	 * @name removeFilter
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {object} filter The filter to delete
	 * @description Remove a filter and update datagrid filters
	 */
	removeFilter(filter) {
		this.FilterService.removeFilter(filter);
		this.StatisticsService.updateFilteredStatistics();
		this._saveFilters();
	}

	/**
	 * @ngdoc method
	 * @name toggleFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Push a filter in the filter list
	 */
	toggleFilters() {
		this.FilterService.toggleFilters();
		this.StatisticsService.updateFilteredStatistics();
	}

	/**
	 * @ngdoc method
	 * @name _saveFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Save filter in the localStorage
	 */
	_saveFilters() {
		this.StorageService.saveFilter(
			this.state.playground.preparation ? this.state.playground.preparation.id : this.state.playground.dataset.id,
			this.state.playground.filter.gridFilters
		);
	}

	addFilter(type, colId, colName, args, removeFilterFn, keyName) {
		this.FilterService.addFilter(type, colId, colName, args, removeFilterFn, keyName);
		this.StatisticsService.updateFilteredStatistics();
		this._saveFilters();
	}

	updateFilter(oldFilter, newValue, keyName) {
		this.FilterService.updateFilter(oldFilter, newValue, keyName);
		this.StatisticsService.updateFilteredStatistics();
		this._saveFilters();
	}
}
