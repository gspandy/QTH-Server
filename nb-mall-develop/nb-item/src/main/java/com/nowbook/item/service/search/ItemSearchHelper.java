package com.nowbook.item.service.search;

import com.nowbook.item.model.Item;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-01-28
 */
public class ItemSearchHelper {
    private final static Splitter splitter = Splitter.on('_').trimResults().omitEmptyStrings();

    public static QueryBuilder composeQuery(Map<String, String> params) {

        QueryBuilder queryBuilder;

        List<FilterBuilder> filters = Lists.newArrayList();

        String keywords = params.get("q");
        if (!Strings.isNullOrEmpty(keywords)) {
            queryBuilder = QueryBuilders.matchQuery("name", keywords);
        } else {
            queryBuilder = QueryBuilders.matchAllQuery();
        }
        String userId = params.get("userId");
        if (!Strings.isNullOrEmpty(userId)) {
            filters.add(FilterBuilders.termFilter("userId", userId));
        }
        String regions = params.get("regions");
        /*if(!Strings.isNullOrEmpty(regions)) {
            List<String> parsingRegions = splitter.splitToList(regions);
            filters.add(FilterBuilders.inFilter("regionIds", parsingRegions.toArray(new String[parsingRegions.size()])));
        }*/

        String siteId = params.get("siteId");
        if (!Strings.isNullOrEmpty(siteId)) {
            filters.add(FilterBuilders.termFilter("siteId", Long.parseLong(siteId)));
        }
        //改动店铺shopId   dpzh  2017-7-24
//        //剔除shopId为0的预售商品
//        String shopId = params.get("shopId");
//        if(!Strings.isNullOrEmpty(shopId)&&Objects.equal(shopId, "0")) {
//            filters.add(FilterBuilders.notFilter(FilterBuilders.termFilter("shopId", Long.parseLong(shopId))));
//        }
        String priceType = params.get("priceType");
        if(!Strings.isNullOrEmpty(priceType)) {
            List<String> priceTypes = splitter.splitToList(priceType);
            filters.add(FilterBuilders.inFilter("priceType", priceTypes.toArray(new String[priceTypes.size()])));
        }


        String shopId = params.get("shopId");
        if(!Strings.isNullOrEmpty(shopId)) {
            params.put("shopId", shopId);
        }

        String priceFrom = params.get("p_f");
        String priceTo = params.get("p_t");
        if (!Strings.isNullOrEmpty(priceFrom) || !Strings.isNullOrEmpty(priceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("price");
            if (!Strings.isNullOrEmpty(priceFrom)) {
                Double price = Double.parseDouble(priceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(priceTo)) {
                Double price = Double.parseDouble(priceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }

        //设置五个价格---准天使
        String quasiAngelPriceFrom = params.get("p_q_a_f");
        String quasiAngelPriceTo = params.get("p_q_a_t");
        if (!Strings.isNullOrEmpty(quasiAngelPriceFrom) || !Strings.isNullOrEmpty(quasiAngelPriceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("quasiAngelPrice");
            if (!Strings.isNullOrEmpty(quasiAngelPriceFrom)) {
                Double price = Double.parseDouble(quasiAngelPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(quasiAngelPriceTo)) {
                Double price = Double.parseDouble(quasiAngelPriceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }

        //设置五个价格---天使
        String angelPriceFrom = params.get("p_a_f");
        String angelPriceFromTo = params.get("p_a_t");
        if (!Strings.isNullOrEmpty(angelPriceFrom) || !Strings.isNullOrEmpty(angelPriceFromTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("angelPrice");
            if (!Strings.isNullOrEmpty(angelPriceFrom)) {
                Double price = Double.parseDouble(angelPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(angelPriceFromTo)) {
                Double price = Double.parseDouble(angelPriceFromTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }


        //设置五个价格---金卡价格
        String goldPriceFrom = params.get("p_g_f");
        String goldPriceTo = params.get("p_g_t");
        if (!Strings.isNullOrEmpty(goldPriceFrom) || !Strings.isNullOrEmpty(goldPriceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("goldPrice");
            if (!Strings.isNullOrEmpty(goldPriceFrom)) {
                Double price = Double.parseDouble(goldPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(goldPriceTo)) {
                Double price = Double.parseDouble(goldPriceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }


        //设置五个价格---铂金价格
        String platinumPriceFrom = params.get("p_p_f");
        String platinumPriceTo = params.get("p_p_t");
        if (!Strings.isNullOrEmpty(platinumPriceFrom) || !Strings.isNullOrEmpty(platinumPriceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("platinumPrice");
            if (!Strings.isNullOrEmpty(platinumPriceFrom)) {
                Double price = Double.parseDouble(platinumPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(platinumPriceTo)) {
                Double price = Double.parseDouble(platinumPriceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }

        //设置五个价格---黑卡价格
        String blackPriceFrom = params.get("p_b_f");
        String blackPriceTo = params.get("p_b_t");
        if (!Strings.isNullOrEmpty(blackPriceFrom) || !Strings.isNullOrEmpty(blackPriceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("blackPrice");
            if (!Strings.isNullOrEmpty(blackPriceFrom)) {
                Double price = Double.parseDouble(blackPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(blackPriceTo)) {
                Double price = Double.parseDouble(blackPriceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }

        //设置五个价格---合伙人价格
        String partnersPriceFrom = params.get("p_p_s_f");
        String partnersPriceTo = params.get("p_p_s_t");
        if (!Strings.isNullOrEmpty(partnersPriceFrom) || !Strings.isNullOrEmpty(partnersPriceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("partnerPrice");
            if (!Strings.isNullOrEmpty(partnersPriceFrom)) {
                Double price = Double.parseDouble(partnersPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(partnersPriceTo)) {
                Double price = Double.parseDouble(partnersPriceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }

        //设置五个价格---公司价格
        String comPriceFrom = params.get("p_c_f");
        String comPriceTo = params.get("p_c_t");
        if (!Strings.isNullOrEmpty(comPriceFrom) || !Strings.isNullOrEmpty(comPriceTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("comPrice");
            if (!Strings.isNullOrEmpty(comPriceFrom)) {
                Double price = Double.parseDouble(comPriceFrom) * 100;
                range.from(price.intValue());
            }
            if (!Strings.isNullOrEmpty(comPriceTo)) {
                Double price = Double.parseDouble(comPriceTo) * 100;
                range.to(price.intValue());
            }
            filters.add(range);
        }




        String quantityFrom = params.get("q_f");
        String quantityTo = params.get("q_t");
        if (!Strings.isNullOrEmpty(quantityFrom) || !Strings.isNullOrEmpty(quantityTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("quantity");
            if (!Strings.isNullOrEmpty(quantityFrom)) {
                range.from(Integer.parseInt(quantityFrom));
            }

            if (!Strings.isNullOrEmpty(quantityTo)) {
                range.to(Integer.parseInt(quantityTo));
            }
            filters.add(range);
        }

        String soldQuantityFrom = params.get("s_f");
        String soldQuantityTo = params.get("s_t");
        if (!Strings.isNullOrEmpty(soldQuantityFrom) || !Strings.isNullOrEmpty(soldQuantityTo)) {
            RangeFilterBuilder range = FilterBuilders.rangeFilter("soldQuantity");
            if (!Strings.isNullOrEmpty(soldQuantityFrom)) {
                range.from(Integer.parseInt(soldQuantityFrom));
            }

            if (!Strings.isNullOrEmpty(soldQuantityTo)) {
                range.to(Integer.parseInt(soldQuantityTo));
            }
            filters.add(range);
        }

        String status = params.get("status");
        if (!Strings.isNullOrEmpty(status)) {
            filters.add(FilterBuilders.termFilter("status", Integer.parseInt(status)));
        }
        Long categoryId = !Strings.isNullOrEmpty(params.get("cid")) ? Long.valueOf(params.get("cid")) : null;
        if (categoryId != null && !Objects.equal(categoryId, 0L)) { // category id 0 means search all categories
            filters.add(FilterBuilders.termFilter("categoryIds", categoryId));
        }
        String attributeIds = params.get("pvids");
        if (!Strings.isNullOrEmpty(attributeIds)) {

            Iterable<Long> attributes = Iterables.transform(splitter.split(attributeIds), new Function<String, Long>() {
                @Override
                public Long apply(String input) {
                    return Long.valueOf(input);
                }
            });
            for (Long attribute : attributes) {
                filters.add(FilterBuilders.termFilter("attributeIds", attribute));
            }
        }
        //店铺内类目搜索
        String tag = params.get("tag");
        if (!Strings.isNullOrEmpty(tag)) {
            filters.add(FilterBuilders.termFilter("tags", tag));
        }
        //店铺内手动推荐
        String ids = params.get("ids");
        if (!Strings.isNullOrEmpty(ids)) {
            List<String> idStrings = splitter.splitToList(ids);
            filters.add(FilterBuilders.idsFilter(Item.class.getSimpleName().toLowerCase()).addIds(idStrings.toArray(new String[idStrings.size()])));
        }
        //根据spuId来搜索
        String spuId = params.get("spuId");
        if(!Strings.isNullOrEmpty(spuId)) {
            filters.add(FilterBuilders.termFilter("spuId", spuId));
        }
        //根据品牌id搜索
        String brandId = params.get("bid");
        if(!Strings.isNullOrEmpty(brandId)) {
            filters.add(FilterBuilders.termFilter("brandId", brandId));
        }

        //根据多个品牌id搜索
        String bids = params.get("bids");
        if(!Strings.isNullOrEmpty(bids)) {
            List<String> brandIds = splitter.splitToList(bids);
            filters.add(FilterBuilders.inFilter("brandId", brandIds.toArray(new String[brandIds.size()])));
        }

        //根据多个categoryIds搜索
        String categoryIds = params.get("categoryIds");
        if(!Strings.isNullOrEmpty(categoryIds)) {
            List<String> parsingCids = splitter.splitToList(categoryIds);
            filters.add(FilterBuilders.inFilter("categoryIds", parsingCids.toArray(new String[parsingCids.size()])));
        }

        if (filters.isEmpty()) {
            return queryBuilder;
        } else {
            AndFilterBuilder and = new AndFilterBuilder();
            for (FilterBuilder filter : filters) {
                and.add(filter);
            }
            return new FilteredQueryBuilder(queryBuilder, and);
        }
    }

    public static void composeSort(SearchRequestBuilder requestBuilder, String sort) {
        if (!Strings.isNullOrEmpty(sort)) {
            Iterable<String> parts = splitter.split(sort);
            String price = Iterables.getFirst(parts, "0");
            String quantity = Iterables.get(parts, 1, "0");
            String soldQuantity = Iterables.get(parts, 2, "0");
            String createdAt = Iterables.get(parts, 3, "0");
            switch (Integer.valueOf(price)) {
                case 1:
                    requestBuilder.addSort("price", SortOrder.ASC);
                    break;
                case 2:
                    requestBuilder.addSort("price", SortOrder.DESC);
                    break;
                default:
                    break;
            }
            switch (Integer.valueOf(quantity)) {
                case 1:
                    requestBuilder.addSort("quantity", SortOrder.ASC);
                    break;
                case 2:
                    requestBuilder.addSort("quantity", SortOrder.DESC);
                    break;
                default:
                    break;
            }
            switch (Integer.valueOf(soldQuantity)) {
                case 1:
                    requestBuilder.addSort("soldQuantity", SortOrder.ASC);
                    break;
                case 2:
                    requestBuilder.addSort("soldQuantity", SortOrder.DESC);
                    break;
                default:
                    break;
            }
            switch (Integer.valueOf(createdAt)) {
                case 1:
                    requestBuilder.addSort("createdAt", SortOrder.ASC);
                    break;
                case 2:
                    requestBuilder.addSort("createdAt", SortOrder.DESC);
                    break;
                default:
                    break;
            }
        }
    }
}
