package com.nowbook.item.service;

import com.nowbook.common.model.Response;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.item.dto.ItemsWithTagFacets;
import com.nowbook.item.model.Item;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.User;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yangzefeng
 * Date: 13-11-15
 * Time: 下午3:19
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classPath:/search-context.xml",
//                                   "classPath:/search-context-test.xml"})
public abstract class ItemSearchServiceImplTest {

    private BaseUser user;

    private Map<String, String> params;

    //@Autowired
    private ItemSearchServiceImpl itemSearchService;

    private void setUp() {
        JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();
        Item item = new Item();
        item.setId(1L);
        item.setUserId(1L);
        item.setName("端点咖啡小屋");
        item.setPrice(99);
        String source = jsonMapper.toJson(item);

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "nowbook").build();
        Client client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("102.nowbook.io", 9300));
        client.prepareIndex("items", "item", String.valueOf((Object) item.getId())).setSource(source).execute().actionGet();

        user = new User();
        user.setId(1L);
        params.put("p_f", "1");
        params.put("p_t", "100");
    }

    @Test
    public void testSearchOnShelfItemsInShop() throws Exception {
        Response<ItemsWithTagFacets> response = itemSearchService.searchOnShelfItemsInShop(1, 20, params);
    }

    @Test
    public void testFacetSearchItem() throws Exception {

    }
}
