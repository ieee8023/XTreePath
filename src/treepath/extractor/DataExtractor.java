package treepath.extractor;

import java.util.Collection;
import java.util.Map;

import treepath.util.DataModel;

public interface DataExtractor {

	void train(Collection<DataModel> training) throws Exception;

	Map<String, String> extract(DataModel d) throws Exception;

}
