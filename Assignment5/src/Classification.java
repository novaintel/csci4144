

import java.util.Collection;


public class Classification<T, K> {


    Collection<T> featureset;


    K category;


    double probability;


    public Classification(Collection<T> featureset, K category) {
        this(featureset, category, 1.0f);
    }


    public Classification(Collection<T> featureset, K category,
            double probability) {
        this.featureset = featureset;
        this.category = category;
        this.probability = probability;
    }


    public Collection<T> getFeatureset() {
        return featureset;
    }

 
    public double getProbability() {
        return this.probability;
    }


    public K getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Classification category=" + this.category
                + ", probability=" + this.probability
                + ", featureset=" + this.featureset + "\n";
    }

}
