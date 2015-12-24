package coinbase;

import java.util.Arrays;

public class RawCandle {

	Double[] numbers;
	
	/* From https://docs.exchange.coinbase.com/#get-historic-rates:
	 * Each bucket is an array of the following information:
		time 	bucket start time
		low 	lowest price during the bucket interval
		high 	highest price during the bucket interval
		open 	opening price (first trade) in the bucket interval
		close 	closing price (last trade) in the bucket interval
		volume 	volume of trading activity during the bucket interval
		
		(Time appears to be in millis since the epoch)*/
	
	RawCandle(Double[] numbers) {
		this.numbers = numbers;
	}
	
	public String toString() {
		return Arrays.toString(numbers);
	}
	
}
