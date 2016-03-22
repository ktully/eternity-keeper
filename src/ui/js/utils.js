/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2016 the authors.
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function flattenObject (obj, levels) {
	var results = [];
	var recursiveFlatten = (key, obj, guessLevel, currentLevel, targetLevel) => {
		for (var p in obj) {
			if (!obj.hasOwnProperty(p)) {
				continue;
			}

			var newKey = (key === null) ? p : key + '.' + p;
			if ((guessLevel && typeof obj[p] !== 'object')
				|| (currentLevel !== undefined && currentLevel === targetLevel)) {

				results.push([newKey, obj[p]]);
				continue;
			}

			recursiveFlatten(newKey, obj[p], guessLevel, currentLevel + 1, targetLevel);
		}
	};

	if (typeof obj !== 'object') {
		console.error('1st argument to flattenObject must be of type object, was:', obj);
		return;
	}

	if (levels !== undefined && typeof levels !== 'number') {
		console.error('2nd argument to flattenObject must be of type number, was:', levels);
		return;
	}

	if (levels < 1) {
		console.error('2nd argument to flattenObject must be at least 1, was:', levels);
		return;
	}

	if (levels === undefined) {
		recursiveFlatten(null, obj, true);
	} else {
		recursiveFlatten(null, obj, false, 1, levels);
	}

	return results;
}
