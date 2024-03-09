package apipClass;

import NaSaRpcClient.GetBlockchainInfo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import constants.Constants;
import constants.IndicesNames;
import constants.Strings;
import esTools.NewEsClient;
import fcTools.ParseTools;
import fchClass.Block;
import javaTools.JsonTools;
import javaTools.NumberTools;

import java.io.IOException;
import java.util.*;

public class FreecashInfo {
    public static final long MAX_REQUEST_COUNT = 1000;
    public static final long DEFAULT_COUNT = 100;
    private long height;
    private String blockId;
    private String circulating;
    private String difficulty;
    private String hashRate;
    private String chainSize;
    private String year;
    private String coinbaseMine;
    private String coinbaseFund;
    private String daysToNextYear;
    private String heightOfNextYear;
    private final String daysPerYear = Constants.DAYS_PER_YEAR;
    private final String mineMutualDays = Constants.MINE_MUTUAL_DAYS;
    private final String fundMutualDays = Constants.FUND_MUTUAL_DAYS;
    private final String blockTimeMinute = Constants.BLOCK_TIME_MINUTE;
    private final String initialCoinbaseMine= Constants.INITIAL_COINBASE_MINE;
    private final String initialCoinbaseFund= Constants.INITIAL_COINBASE_FUND;
    private final long startTime = Constants.START_TIME;
    private final String genesisBlockId = Constants.GENESIS_BLOCK_ID;

    public static void main(String[] args) throws IOException {

        long height1 = 2000000;
        FreecashInfo freecashInfo = new FreecashInfo();
        freecashInfo.infoBest("http://localhost:8332","username","password");
        System.out.println(freecashInfo.toNiceJson());

        FreecashInfo freecashInfo1 = new FreecashInfo();
        NewEsClient newEsClient = new NewEsClient();
        ElasticsearchClient esClient = newEsClient.getSimpleEsClient();
        freecashInfo1.infoByHeight(height1,esClient);
        System.out.println(freecashInfo1.toNiceJson());

        Map<Long, String> timeDiffMap = difficultyHistory(0, 1704321137,100 ,esClient);
        JsonTools.gsonPrint(timeDiffMap);

        Map<Long, String> timeHashRateMap = hashRateHistory(0, 1704321137,18 ,esClient);
        JsonTools.gsonPrint(timeHashRateMap);

        Map<Long, Long> blockTimefMap = blockTimeHistory(0, 1704321137,1000 ,esClient);

        System.out.println(timeDiffMap.size());
        System.out.println(timeHashRateMap.size());
        System.out.println(blockTimefMap.size());

        newEsClient.shutdownClient();
    }

    public String toNiceJson(){
        return JsonTools.getNiceString(this);
    }

    public static Map<Long,Long> blockTimeHistory(long startTime, long endTime, long count, ElasticsearchClient esClient){
        if(count>0)count += 1;
        else count = DEFAULT_COUNT+1;
        if(count>MAX_REQUEST_COUNT)count=MAX_REQUEST_COUNT;
        SearchResponse<Block> result = getBlockListHistory(startTime, endTime, count,esClient);
        if (result == null) return null;

        Map<Long,Long> timeTimeMap = new HashMap<>();
        long lastBlockTime = 0;
        long lastHeight = 0;

        for(Hit<Block> hit:result.hits().hits()){
            Block block = hit.source();
            if(block==null)continue;
            if(lastBlockTime!=0){
                timeTimeMap.put(block.getTime(),(block.getTime()-lastBlockTime)/(block.getHeight()-lastHeight));
            }
            lastBlockTime = block.getTime();
            lastHeight = block.getHeight();
        }
        return timeTimeMap;
    }


    public static Map<Long,String> difficultyHistory(long startTime, long endTime, long count, ElasticsearchClient esClient){

        SearchResponse<Block> result = getBlockListHistory(startTime, endTime, count,esClient);
        if (result == null) return null;

        Map<Long,String> timeDiffMap = new LinkedHashMap<>();

        for(Hit<Block> hit:result.hits().hits()){
            Block block = hit.source();
            if(block==null)continue;
            String diff = NumberTools.numberToPlainString(String.valueOf(ParseTools.bitsToDifficulty(block.getBits())),"3");
            timeDiffMap.put(block.getTime(),diff);
        }
        return timeDiffMap;
    }

    public static Map<Long,String> hashRateHistory(long startTime, long endTime, long count, ElasticsearchClient esClient){
        SearchResponse<Block> result = getBlockListHistory(startTime, endTime, count,esClient);
        if (result == null) return null;

        Map<Long,String> timeHashRateMap = new LinkedHashMap<>();

        for(Hit<Block> hit:result.hits().hits()){
            Block block = hit.source();
            if(block==null)continue;
            String hashRate = NumberTools.numberToPlainString(String.valueOf(ParseTools.bitsToHashRate(block.getBits())),"0");
            timeHashRateMap.put(block.getTime(),hashRate);
        }
        return timeHashRateMap;
    }

    private static SearchResponse<Block> getBlockListHistory(long startTime, long endTime, long count, ElasticsearchClient esClient) {
        if(count>MAX_REQUEST_COUNT)count=MAX_REQUEST_COUNT;
        if(count <=0)count=DEFAULT_COUNT;
        if(startTime==0)startTime=Constants.START_TIME;
        if(endTime==0)endTime=System.currentTimeMillis()/1000;

        List<FieldValue> heightList = new ArrayList<>();
        if(startTime< Constants.START_TIME)startTime= Constants.START_TIME;
        long startHeight = estimateHeight(startTime);
        long endHeight = estimateHeight(endTime);
        long step = (endHeight-startHeight) / count;

        long height = startHeight;

        while (height<endHeight) {
            heightList.add(FieldValue.of(height));
            height +=step;
        }
        heightList.add(FieldValue.of(endHeight));
        SearchResponse<Block> result;

        try {
            result = esClient.search(s->s.index(IndicesNames.BLOCK)
                            .size(heightList.size())
                            .sort(s1->s1.field(f->f.field(Strings.HEIGHT)
                                    .order(SortOrder.Asc)))
                            .query(q->q.terms(t->t
                                    .field(Strings.HEIGHT)
                                    .terms(t1->t1.value(heightList))))
                    ,Block.class);
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    private static long estimateHeight(long startTime) {
        if(startTime< Constants.START_TIME)return -1;
        return (startTime - Constants.START_TIME) / 60;
    }

    public void infoBest(String fchRpcUrl, String username, String password){
        GetBlockchainInfo getBlockchainInfo = new GetBlockchainInfo();
        GetBlockchainInfo.BlockchainInfo blockchainInfo = getBlockchainInfo.getBlockchainInfo(fchRpcUrl,username,password);
        this.height=blockchainInfo.getBlocks();
        this.blockId=blockchainInfo.getBestblockhash();

        this.difficulty=NumberTools.numberToPlainString(String.valueOf(blockchainInfo.getDifficulty()),"0");
        double hashRate = ParseTools.difficultyToHashRate(blockchainInfo.getDifficulty());
        this.hashRate=NumberTools.numberToPlainString(String.valueOf(hashRate),"0");
        this.chainSize= NumberTools.numberToPlainString(String.valueOf(blockchainInfo.getSize_on_disk()),null);

        infoByHeight(this.height);

    }
    public void infoByHeight(long height, ElasticsearchClient esClient){
        this.height=height;
        Block block;
        try {
            SearchResponse<Block> result = esClient.search(s -> s.index(IndicesNames.BLOCK).query(q -> q.term(t -> t.field(Strings.HEIGHT).value(height))), Block.class);
            if(result.hits().total()!=null && result.hits().total().value()!=0){
                block = result.hits().hits().get(0).source();
                double difficulty = ParseTools.bitsToDifficulty(block.getBits());
                double hashRate = ParseTools.difficultyToHashRate(difficulty);
                this.difficulty=NumberTools.numberToPlainString(String.valueOf(difficulty),"0");
                this.hashRate=NumberTools.numberToPlainString(String.valueOf(hashRate),"0");
                this.blockId=block.getBlockId();
            }
        } catch (IOException e) {
            System.out.println("Failed to get block information from ES.");
        }
        infoByHeight(height);
    }
    public void infoByHeight(long height){

        double circulating = 0;
        double coinbaseMine = 25;
        double coinbaseFund = 25;
        long blockPerYear = Long.parseLong(Constants.DAYS_PER_YEAR)*24*60;
        height = height+1;

        long years = height / blockPerYear;

        for(int i=0;i<years;i++){
            circulating += blockPerYear * (coinbaseMine+coinbaseFund);
            if(years<40) {
                coinbaseMine *= 0.8;
                coinbaseFund *= 0.5;
            }
        }
        circulating += height % blockPerYear * (coinbaseMine+coinbaseFund);

        this.circulating=NumberTools.numberToPlainString(String.valueOf(circulating),"0");
        this.year= String.valueOf(years+1);
        this.coinbaseMine=String.valueOf(coinbaseMine);
        this.coinbaseFund=String.valueOf(coinbaseFund);
        long blocksRemainingThisYear = blockPerYear - height % blockPerYear;
        long daysToNextYear = blocksRemainingThisYear / (24 * 60);
        this.daysToNextYear=String.valueOf(daysToNextYear);
        heightOfNextYear=String.valueOf(blocksRemainingThisYear+height);

    }

    public String getCirculating() {
        return circulating;
    }

    public void setCirculating(String circulating) {
        this.circulating = circulating;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getHashRate() {
        return hashRate;
    }

    public void setHashRate(String hashRate) {
        this.hashRate = hashRate;
    }

    public long estimateHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getChainSize() {
        return chainSize;
    }

    public void setChainSize(String chainSize) {
        this.chainSize = chainSize;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCoinbaseMine() {
        return coinbaseMine;
    }

    public void setCoinbaseMine(String coinbaseMine) {
        this.coinbaseMine = coinbaseMine;
    }

    public String getCoinbaseFund() {
        return coinbaseFund;
    }

    public void setCoinbaseFund(String coinbaseFund) {
        this.coinbaseFund = coinbaseFund;
    }

    public String getDaysToNextYear() {
        return daysToNextYear;
    }

    public void setDaysToNextYear(String daysToNextYear) {
        this.daysToNextYear = daysToNextYear;
    }

    public String getHeightOfNextYear() {
        return heightOfNextYear;
    }

    public void setHeightOfNextYear(String heightOfNextYear) {
        this.heightOfNextYear = heightOfNextYear;
    }

    public String getDaysPerYear() {
        return Constants.DAYS_PER_YEAR;
    }

    public String getMineMutualDays() {
        return Constants.MINE_MUTUAL_DAYS;
    }

    public String getFundMutualDays() {
        return Constants.FUND_MUTUAL_DAYS;
    }

    public String getBlockTimeMinute() {
        return Constants.BLOCK_TIME_MINUTE;
    }

    public String getInitialCoinbaseMine() {
        return Constants.INITIAL_COINBASE_MINE;
    }

    public String getInitialCoinbaseFund() {
        return Constants.INITIAL_COINBASE_FUND;
    }

    public long getStartTime() {
        return Constants.START_TIME;
    }

    public long getHeight() {
        return height;
    }

    public String getGenesisBlockId() {
        return genesisBlockId;
    }
}
