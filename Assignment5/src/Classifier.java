

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


public abstract class Classifier<T, K> implements FeatureProbabilityInterface<T, K> {

	Dictionary<K, Dictionary<T, Integer>> featureCountPerCategory;


	Dictionary<T, Integer> featureCount;


	Dictionary<K, Integer> categoryCount;


	Queue<Classification<T, K>> memoryQueue;

	public Classifier() {
		this.reset();
	}

	public void reset() {
		this.featureCountPerCategory =
				new Hashtable<K, Dictionary<T,Integer>>();
		this.featureCount =
				new Hashtable<T, Integer>();
		this.categoryCount =
				new Hashtable<K, Integer>();
		this.memoryQueue = new LinkedList<Classification<T, K>>();
	}


	public Set<T> getFeatures() {
		return ((Hashtable<T, Integer>) this.featureCount).keySet();
	}


	public Set<K> getCategories() {
		return ((Hashtable<K, Integer>) this.categoryCount).keySet();
	}


	public int getCategoriesTotal() {
		int toReturn = 0;
		for (Enumeration<Integer> e = this.categoryCount.elements();
				e.hasMoreElements();) {
			toReturn += e.nextElement();
		}
		return toReturn;
	}

	public void incrementFeature(T feature, K category) {
		Dictionary<T, Integer> features =
				this.featureCountPerCategory.get(category);
		if (features == null) {
			this.featureCountPerCategory.put(category,
					new Hashtable<T, Integer>());
			features = this.featureCountPerCategory.get(category);
		}
		Integer count = features.get(feature);
		if (count == null) {
			features.put(feature, 0);
			count = features.get(feature);
		}
		features.put(feature, ++count);

		Integer totalCount = this.featureCount.get(feature);
		if (totalCount == null) {
			this.featureCount.put(feature, 0);
			totalCount = this.featureCount.get(feature);
		}
		this.featureCount.put(feature, ++totalCount);
	}


	public void incrementCategory(K category) {
		Integer count = this.categoryCount.get(category);
		if (count == null) {
			this.categoryCount.put(category, 0);
			count = this.categoryCount.get(category);
		}
		this.categoryCount.put(category, ++count);
	}


	public void decrementFeature(T feature, K category) {
		Dictionary<T, Integer> features =
				this.featureCountPerCategory.get(category);
		if (features == null) {
			return;
		}
		Integer count = features.get(feature);
		if (count == null) {
			return;
		}
		if (count.intValue() == 1) {
			features.remove(feature);
			if (features.size() == 0) {
				this.featureCountPerCategory.remove(category);
			}
		} else {
			features.put(feature, --count);
		}

		Integer totalCount = this.featureCount.get(feature);
		if (totalCount == null) {
			return;
		}
		if (totalCount.intValue() == 1) {
			this.featureCount.remove(feature);
		} else {
			this.featureCount.put(feature, --totalCount);
		}
	}


	public void decrementCategory(K category) {
		Integer count = this.categoryCount.get(category);
		if (count == null) {
			return;
		}
		if (count.intValue() == 1) {
			this.categoryCount.remove(category);
		} else {
			this.categoryCount.put(category, --count);
		}
	}


	public int featureCount(T feature, K category) {
		Dictionary<T, Integer> features =
				this.featureCountPerCategory.get(category);
		if (features == null)
			return 0;
		Integer count = features.get(feature);
		if(count == null)
			return 0;
		else
			return count.intValue();
	}


	public int categoryCount(K category) {
		Integer count = this.categoryCount.get(category);
		if(count == null)
			return 0;
		else 
			return count.intValue();
	}


	@Override
	public double featureProbability(T feature, K category) {
		if (this.categoryCount(category) == 0)
			return 0;
		return (double) this.featureCount(feature, category)
				/ (double) this.categoryCount(category);
	}


	public double featureWeighedAverage(T feature, K category) {
		return this.featureWeighedAverage(feature, category,
				null, 1.0f, 0.5f);
	}


	public double featureWeighedAverage(T feature, K category,
			FeatureProbabilityInterface<T, K> calculator) {
		return this.featureWeighedAverage(feature, category,
				calculator, 1.0f, 0.5f);
	}


	public double featureWeighedAverage(T feature, K category,
			FeatureProbabilityInterface<T, K> calculator, double weight) {
		return this.featureWeighedAverage(feature, category,
				calculator, weight, 0.5f);
	}


	public double featureWeighedAverage(T feature, K category,
			FeatureProbabilityInterface<T, K> calculator, double weight,
			double assumedProbability) {

		final double basicProbability;
		if (calculator == null)
			basicProbability =  this.featureProbability(feature, category);
		else
			basicProbability = calculator.featureProbability(feature, category);
		Integer totals = this.featureCount.get(feature);
		if (totals == null)
			totals = 0;
		return (weight * assumedProbability + totals  * basicProbability)
				/ (weight + totals);
	}

	public void learn(K category, Collection<T> features) {
		this.learn(new Classification<T, K>(features, category));
	}

	public void learn(Classification<T, K> classification) {

		for (T feature : classification.getFeatureset())
			this.incrementFeature(feature, classification.getCategory());
		this.incrementCategory(classification.getCategory());

		this.memoryQueue.offer(classification);
	}


	public abstract Classification<T, K> classify(Collection<T> features);

}
