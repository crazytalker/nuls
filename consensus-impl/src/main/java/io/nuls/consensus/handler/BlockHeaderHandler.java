package io.nuls.consensus.handler;

import io.nuls.consensus.entity.AskSmallBlockData;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockHeaderHandler extends AbstractEventBusHandler<BlockHeaderEvent> {

    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    @Override
    public void onEvent(BlockHeaderEvent event, String fromId) {
        if (DistributedBlockInfoRequestUtils.getInstance().addBlockHeader(fromId, event.getEventBody())) {
            return;
        }
        BlockHeader header = event.getEventBody();
        //todo 分叉处理
        header.verify();
        headerCacheService.cacheHeader(header);
        GetSmallBlockEvent smallBlockEvent = new GetSmallBlockEvent();
        AskSmallBlockData data = new AskSmallBlockData();
        data.setBlockHash(header.getHash());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (NulsDigestData txHash : header.getTxHashList()) {
            boolean exist = ledgerService.txExist(txHash.getDigestHex());
            if (!exist) {
                txHashList.add(txHash);
            }
        }
        data.setTxHashList(txHashList);
        smallBlockEvent.setEventBody(data);
        eventBroadcaster.sendToPeer(smallBlockEvent, fromId);
    }
}
