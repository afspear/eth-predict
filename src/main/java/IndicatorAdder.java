import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.PPOIndicator;
import eu.verdelhan.ta4j.indicators.ROCIndicator;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.SMAIndicator;
import eu.verdelhan.ta4j.indicators.WilliamsRIndicator;
import eu.verdelhan.ta4j.indicators.helpers.AverageTrueRangeIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.helpers.PriceVariationIndicator;
import eu.verdelhan.ta4j.indicators.helpers.TypicalPriceIndicator;
import eu.verdelhan.ta4j.indicators.statistics.StandardDeviationIndicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class IndicatorAdder {
    public static List<Indicator> addIndicators(TimeSeries series) {
        // Close price
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        // Typical price
        TypicalPriceIndicator typicalPrice = new TypicalPriceIndicator(series);
        // Price variation
        PriceVariationIndicator priceVariation = new PriceVariationIndicator(series);
        // Simple moving averages
        SMAIndicator shortSma = new SMAIndicator(closePrice, 8);
        SMAIndicator longSma = new SMAIndicator(closePrice, 20);
        // Exponential moving averages
        EMAIndicator shortEma = new EMAIndicator(closePrice, 8);
        EMAIndicator longEma = new EMAIndicator(closePrice, 20);
        // Percentage price oscillator
        PPOIndicator ppo = new PPOIndicator(closePrice, 12, 26);
        // Rate of change
        ROCIndicator roc = new ROCIndicator(closePrice, 100);
        // Relative strength index
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        // Williams %R
        WilliamsRIndicator williamsR = new WilliamsRIndicator(series, 20);
        // Average true range
        AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, 20);
        // Standard deviation
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 14);

        final int nbTicks = series.getTickCount();
        StringBuilder sb = new StringBuilder(
         /* "timestamp, */"closeNextDay,open,high,low,close,typical,variation,sma8,sma20,ema8,ema20,ppo,roc,rsi,williamsr,atr,sd\n");


        for (int i = 0; i < nbTicks; i++) {
            String nextClose;
            try {
                nextClose = series.getTick(i+1).getClosePrice().toString();
            } catch (IndexOutOfBoundsException e) {
                nextClose = "";
            }


            sb
              //.append(series.getTick(i).getEndTime()).append(',')
              .append(nextClose).append(',')
              .append(series.getTick(i).getOpenPrice()).append(',')
              .append(series.getTick(i).getMaxPrice()).append(',')
              .append(series.getTick(i).getMinPrice()).append(',')
              .append(closePrice.getValue(i)).append(',')
              .append(typicalPrice.getValue(i)).append(',')
              .append(priceVariation.getValue(i)).append(',')
              .append(shortSma.getValue(i)).append(',')
              .append(longSma.getValue(i)).append(',')
              .append(shortEma.getValue(i)).append(',')
              .append(longEma.getValue(i)).append(',')
              .append(ppo.getValue(i)).append(',')
              .append(roc.getValue(i)).append(',')
              .append(rsi.getValue(i)).append(',')
              .append(williamsR.getValue(i)).append(',')
              .append(atr.getValue(i)).append(',')
              .append(sd.getValue(i)).append('\n');
        }

        try {
            Files.write(Paths.get("dataWithIndicators.csv"), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
