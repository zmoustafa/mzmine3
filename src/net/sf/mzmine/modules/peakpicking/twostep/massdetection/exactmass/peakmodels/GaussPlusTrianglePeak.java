/*
 * Copyright 2006-2008 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peakpicking.twostep.massdetection.exactmass.peakmodels;

import net.sf.mzmine.modules.peakpicking.twostep.massdetection.exactmass.PeakModel;
import net.sf.mzmine.util.Range;

public class GaussPlusTrianglePeak implements PeakModel {

	private float mzMain, intensityMain, FWHM;
	private double partA, angle;
	private Range rangePeakAtFivePercentage;

	/**
	 * @see net.sf.mzmine.modules.peakpicking.twostep.massdetection.exactmass.peakmodel.PeakModel#setParameters(float,
	 *      float, float)
	 */
	public float getIntensity(float mz) {

		// Using the Gaussian function we calculate the intensity m/z given (mz)
		if ((mz >= rangePeakAtFivePercentage.getMin())
				&& (mz <= rangePeakAtFivePercentage.getMax())) {
			double partB = -1 * Math.pow((mzMain - mz), 2) / partA;
			float intensity = (float) (intensityMain * Math.exp(partB));
			return intensity;
		} else {
			// We use Pitagora's theorem to calculate the intensity with the
			// function opposite side = adjacent side * Tan (angle)
			float adjacent = 1 - Math.abs(mzMain - mz);
			float intensity = (float) (adjacent * Math.tan(angle));
			return intensity;
		}
	}

	public Range getWidth(float partialIntensity) {
		/*
		 * The height value must be bigger than zero.The zero value is not used
		 * because the Lorentzian function tends to infinite and in this
		 * function with zero intensity we get a NaN. If that is the case we
		 * have a too big range and could result in to make useless comparisons.
		 */

		if (partialIntensity <= 0)
			return new Range(0, Float.MAX_VALUE);

		if (partialIntensity >= intensityMain * 0.05f) {
			double ln = Math.abs(Math.log(partialIntensity));

			// Using the Gaussian function we calculate the peak width at
			// intensity given (partialIntensity),
			float sideRange = (float) Math.sqrt(partA * ln) / 2.0f;
			// This range represents the width of our peak in m/z terms
			Range rangePeak = new Range(mzMain - sideRange, mzMain + sideRange);

			return rangePeak;
		} else {

			// We use Pitagora's theorem to calculate the intensity with the
			// function adjacent side = opposite side / Tan (angle)
			float adjacent = (float) (partialIntensity / Math.tan(angle));
			Range rangePeak = new Range((mzMain - 1 + adjacent),
					(mzMain + 1 - adjacent));
			return rangePeak;
		}

	}

	public void setParameters(float mzMain, float intensityMain,
			float resolution) {
		this.mzMain = mzMain;
		this.intensityMain = intensityMain;

		// FWFM (Full Width at Half Maximum)
		FWHM = mzMain / ((float) resolution);
		partA = 2 * Math.pow(FWHM, 2);

		rangePeakAtFivePercentage = this.getWidth(intensityMain * 0.05f);

		// We use Pitagora's theorem to calculate the angle of a rectangular
		// triangle with a base equal to one (m/z) and height equal to 5%
		// intensity of the peak.
		angle = Math.atan((intensityMain * 0.05));
	}

}
