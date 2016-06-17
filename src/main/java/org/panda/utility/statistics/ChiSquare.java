package org.panda.utility.statistics;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.panda.utility.ArrayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by babur on 4/18/16.
 */
public class ChiSquare
{
	public static double testDependence(long[][] cnts)
	{
		ChiSquareTest cst = new ChiSquareTest();
		return cst.chiSquareTest(cnts);
	}

	public static double testDependence(int[] cat1, int[] cat2)
	{
		return testDependence(ArrayUtil.convertCategoriesToContingencyTable(cat1, cat2));
	}

	public static double testDependence(int[] cat, boolean[] control, boolean[] test)
	{
		return testDependence(ArrayUtil.convertCategorySubsetsToContingencyTables(cat, control, test));
	}

	/**
	 * Calculates the p-value of the given chi-square value with the given degrees of freedom.
	 * @param x chi value
	 * @param n degrees of freedom
	 * @return p-value
	 */
	public static double pValue(double x, double n)
	{
		if(n==1 && x>1000)
		{
			return 0;
		}
		if(x>1000 || n>1000)
		{
			double q = pValue((x - n) * (x - n) / (2 * n), 1) / 2;

			if(x>n)
			{
				return q;
			}
			else
			{
				return 1-q;
			}
		}
		double p = Math.exp(-0.5 * x);
		if((n % 2) == 1)
		{
			p = p * Math.sqrt(2 * x / Math.PI);
		}

		double k = n;

		while(k >= 2)
		{
			p = p * x / k;
			k = k - 2;
		}
		double t = p;
		double a = n;
		while(t > 0.0000000001 * p)
		{
			a = a + 2;
			t = t * x / a;
			p = p + t;
		}

		return 1 - p;
	}


	public static void main(String[] args)
	{
		int n = 2;
		int m = 3;

		List<Double> pvals = new ArrayList<>();

		for (int j = 0; j < 1000; j++)
		{
			long[][] l = new long[n][m];

			Random r = new Random();

			for (int i = 0; i < 100; i++)
			{
				l[r.nextInt(n)][r.nextInt(m)]++;
			}

			double p = testDependence(l);
			if (!Double.isNaN(p)) pvals.add(p);
		}

		UniformityChecker.plot(pvals);
	}
}
