package com.nowbook.web.controller.api.userEvent;

import com.nowbook.sms.SmsService;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.model.OrderLogisticsInfo;
import com.nowbook.trade.model.UserTradeInfo;
import com.nowbook.trade.service.OrderLogisticsInfoService;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.trade.service.UserTradeInfoService;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SmsEventListener {
	private final SmsEventBus eventBus;
	private final OrderQueryService orderQueryService;
	private final AccountService<User> accountService;
	private final OrderLogisticsInfoService orderLogisticsInfoService;
	private final SmsService smsService;
    private UserTradeInfoService userTradeInfoService;

	@Autowired
	public SmsEventListener(SmsEventBus eventBus,
			OrderQueryService orderQueryService,
			AccountService<User> accountService,
            OrderLogisticsInfoService orderLogisticsInfoService,
			SmsService smsService,UserTradeInfoService userTradeInfoService) {
		this.eventBus = eventBus;
		this.orderQueryService = orderQueryService;
		this.accountService = accountService;
		this.orderLogisticsInfoService = orderLogisticsInfoService;
		this.smsService = smsService;
		this.userTradeInfoService = userTradeInfoService;
	}

	@PostConstruct
	public void init() {
		this.eventBus.register(this);
	}

	

	@Subscribe
	public void sendSmsContent(SmsEvent smsEvent) {
		String type=smsEvent.getType();
		if(type.equals("1")){
			Order order=orderQueryService.findById(smsEvent.getOrderId()).getResult();
			User user=accountService.findUserById(order.getSellerId()).getResult();
			User user1=accountService.findUserById(order.getBuyerId()).getResult();
			if(user.getMobile()!=null&&user.getMobile()!=""){							
            if(isUserStatus(String.valueOf(order.getSellerId()),"2")){
            	//sendSms(user.getMobile(),"您有一个新订单【"+smsEvent.getOrderId()+"】，买家【"+user1.getName()+"】已付款，请尽快安排发货。");
				String message = "{\"orderId\":\""+smsEvent.getOrderId()+"\",\"name\":\""+user1.getName()+"\"}";
				//sendSms(user.getMobile(),"您有一个新订单【"+smsEvent.getOrderId()+"】，买家【"+user1.getName()+"】已付款，请尽快安排发货。");
//				sendSms(user.getMobile(),"SMS_4000057",message); //原来代码
				sendSms(user.getMobile(),"SMS_90615010",message);
			}}
		}else if(type.equals("2")){
			Order order=orderQueryService.findById(smsEvent.getOrderId()).getResult();
        	UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
			User user=accountService.findUserById(order.getBuyerId()).getResult();
            OrderLogisticsInfo orderLogisticsInfo=orderLogisticsInfoService.findByOrderId(smsEvent.getOrderId()).getResult();
            if(isUserStatus(String.valueOf(order.getBuyerId()),"1")){
        		//String smsContent="亲爱的【"+user.getName()+"】，您的订单【"+smsEvent.getOrderId()+"】已发货";
				String smsContent="";
        		if(orderLogisticsInfo!=null){
                    if(orderLogisticsInfo.getType().toString().equals("0")){
                       // smsContent="，为您安排【"+orderLogisticsInfo.getExpressName()+"】（快递单号：【"+orderLogisticsInfo.getExpressNo()+"】）送至府上，请检查后签收。";
						smsContent="{\"name\":\""+user.getName()+"\",\"orderId\":\""+smsEvent.getOrderId()+"\",\"expressName\":\""+orderLogisticsInfo.getExpressName()+"\",\"expressNo\":\""+orderLogisticsInfo.getExpressNo()+"\"}";
//						sendSms(userTradeInfo.getPhone(),"SMS_3995073",smsContent);//原来的代码
						sendSms(userTradeInfo.getPhone(),"SMS_90805008",smsContent);
					}else if(orderLogisticsInfo.getType().toString().equals("1")){
						smsContent="{\"name\":\""+user.getName()+"\",\"orderId\":\""+smsEvent.getOrderId()+"\"}";
//						sendSms(userTradeInfo.getPhone(),"SMS_4070001",smsContent);//原来的代码
						sendSms(userTradeInfo.getPhone(),"SMS_90620009",smsContent);
                    }
                }


            }
		}else if(type.equals("3")){
			Order order=orderQueryService.findById(smsEvent.getOrderId()).getResult();
        	UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
			User user=accountService.findUserById(order.getBuyerId()).getResult();
            OrderLogisticsInfo orderLogisticsInfo=orderLogisticsInfoService.findByOrderId(smsEvent.getOrderId()).getResult();
			if(isUserStatus(String.valueOf(order.getBuyerId()),"1")){
          	  List<OrderItem> list=orderQueryService.findOrderItemByOrderId(smsEvent.getOrderId()).getResult();
          		String orderName=list.size()>0?list.get(0).getItemName():"";
          		//String smsContent="亲爱的【"+user.getName()+"】，您的预购商品【"+orderName+"】已发货";
				String smsContent="";
				if(orderLogisticsInfo!=null){
                    if(orderLogisticsInfo.getType().toString().equals("0")){
                       // smsContent="，为您安排【"+orderLogisticsInfo.getExpressName()+"】（快递单号：【"+orderLogisticsInfo.getExpressNo()+"】）送至府上，请检查后签收。";
						smsContent="{\"name\":\""+user.getName()+"\",\"orderName\":\""+orderName+"\",\"expressName\":\""+orderLogisticsInfo.getExpressName()+"\",\"expressNo\":\""+orderLogisticsInfo.getExpressNo()+"\"}";
//						sendSms(userTradeInfo.getPhone(),"SMS_3995073",smsContent);//原来的代码
						sendSms(userTradeInfo.getPhone(),"SMS_90805008",smsContent);
					}else if(orderLogisticsInfo.getType().toString().equals("1")){
                        //smsContent+="，为您安排送至府上，请检查后签收。";
						smsContent="{\"name\":\""+user.getName()+"\",\"orderName\":\""+orderName+"\"}";
//						sendSms(userTradeInfo.getPhone(),"SMS_3975046",smsContent);//原
						sendSms(userTradeInfo.getPhone(),"SMS_90810009",smsContent);
                    }
                }

              }
		}else if(type.equals("4")){
			OrderItem orderItem=orderQueryService.findOrderItemById(smsEvent.getOrderItemId()).getResult();
			long sellerId=orderItem.getSellerId();
            long buyerId=orderItem.getBuyerId();
            long orderId=orderItem.getOrderId();
            User user=accountService.findUserById(sellerId).getResult();
            User user1=accountService.findUserById(buyerId).getResult();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd"); 
            if(user.getMobile()!=null&&user.getMobile()!=""){				
            if(isUserStatus(String.valueOf(sellerId),"2")){
				String smsContent="{\"orderId\":\""+orderId+"\",\"name\":\""+user1.getName()+"\",\"date\":\""+format.format(new Date())+"\"}";
//				sendSms(user.getMobile(),"SMS_4025060",smsContent);//原
				sendSms(user.getMobile(),"SMS_90610008",smsContent);
				//sendSms(user.getMobile(),"您有一项新的业务要处理，贵商铺订单【"+orderId+"】买家【"+user1.getName()+"】"
            	//		+ "于【"+format.format(new Date())+"】申请退款，请尽快审核。");
            } }
		}else if(type.equals("5")){
			OrderItem orderItem=orderQueryService.findOrderItemById(smsEvent.getOrderItemId()).getResult();
			long sellerId=orderItem.getSellerId();
            long buyerId=orderItem.getBuyerId();
            long orderId=orderItem.getOrderId();
            User user=accountService.findUserById(sellerId).getResult();
            User user1=accountService.findUserById(buyerId).getResult();
            if(user.getMobile()!=null&&user.getMobile()!=""){				
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd"); 
            if(isUserStatus(String.valueOf(sellerId),"2")){
            	//sendSms(user.getMobile(),"您有一项新的业务要处理，贵商铺订单【"+orderId+"】买家【"+user1.getName()+"】于"
            	//		+ "【"+format.format(new Date())+"】申请退货，请尽快审核。");
				String smsContent="{\"orderId\":\""+orderId+"\",\"name\":\""+user1.getName()+"\",\"date\":\""+format.format(new Date())+"\"}";
//				sendSms(user.getMobile(),"SMS_4025060",smsContent);//原
				sendSms(user.getMobile(),"SMS_90610008",smsContent);
            }}
		}else if(type.equals("6")){
			OrderItem orderItem=orderQueryService.findOrderItemById(smsEvent.getOrderItemId()).getResult();
			Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
			UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
			long buyerId=orderItem.getBuyerId();
        	User user=accountService.findUserById(buyerId).getResult();
        	if(isUserStatus(String.valueOf(buyerId),"1")){
            	//sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getName()+"】，您的退款申请已审核通过，请留意您的账户信息。");
				String smsContent="{\"name\":\""+user.getName()+"\"}";
//				sendSms(userTradeInfo.getPhone(),"SMS_3975047",smsContent);//原
				sendSms(userTradeInfo.getPhone(),"SMS_90785005",smsContent);
        	}
		}else if(type.equals("7")){
			OrderItem orderItem=orderQueryService.findOrderItemById(smsEvent.getOrderItemId()).getResult();
			Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
			UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
			long buyerId=orderItem.getBuyerId(); 
			User user=accountService.findUserById(buyerId).getResult();
			if(isUserStatus(String.valueOf(buyerId),"1")){
				String smsContent="{\"name\":\""+user.getName()+"\"}";
            	//sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getName()+"】，您的退货申请已审核通过，请留意您的账户信息。");
//				sendSms(userTradeInfo.getPhone(),"SMS_3975047",smsContent);//原
				sendSms(userTradeInfo.getPhone(),"SMS_90785005",smsContent);
			}
		}else if(type.equals("8")){
			 Order order=orderQueryService.findById(smsEvent.getOrderId()).getResult();
         	 User user=accountService.findUserById(order.getSellerId()).getResult();
         	 User user1=accountService.findUserById(order.getBuyerId()).getResult();
         	if(user.getMobile()!=null&&user.getMobile()!=""){				
	           	 if(isUserStatus(String.valueOf(order.getSellerId()),"2")){
					String smsContent="{\"orderId\":\""+smsEvent.getOrderId()+"\",\"name\":\""+user1.getName()+"\"}";
	              //	sendSms(user.getMobile(),
	              	//"您有一个新订单【"+smsEvent.getOrderId()+"】，买家【"+user1.getName()+"】选择货到付款，请尽快安排发货。");
//					 sendSms(user.getMobile(),"SMS_4000057",smsContent);//原
					 sendSms(user.getMobile(),"SMS_90615010",smsContent);
				 }
			}
		}else if(type.equals("9")){
			OrderItem orderItem=orderQueryService.findOrderItemById(smsEvent.getOrderItemId()).getResult();
			long buyerId=orderItem.getBuyerId(); 
			Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
			UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
			User user=accountService.findUserById(buyerId).getResult();
        	if(isUserStatus(String.valueOf(buyerId),"1")){
				String smsContent="{\"name\":\""+user.getName()+"\"}";
            	//sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getName()+"】，您的退款申请已审核通过，请留意您的账户信息。");
//				sendSms(userTradeInfo.getPhone(),"SMS_3975047",smsContent);//原
				sendSms(userTradeInfo.getPhone(),"SMS_90785005",smsContent);
			}
		}else if(type.equals("10")){
			OrderItem orderItem=orderQueryService.findOrderItemById(smsEvent.getOrderItemId()).getResult();
			Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
			long buyerId=orderItem.getBuyerId(); 
        	UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
        	User user=accountService.findUserById(buyerId).getResult();
        	if(isUserStatus(String.valueOf(buyerId),"1")){
				String smsContent="{\"name\":\""+user.getName()+"\"}";
            	//sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getName()+"】，您的退货申请已审核通过，请留意您的账户信息。");
//				sendSms(userTradeInfo.getPhone(),"SMS_3975047",smsContent);//原
				sendSms(userTradeInfo.getPhone(),"SMS_90785005",smsContent);
			}
		}
	}
	private void sendSms(String mobile,String template,String content){
    	smsService.sendSingle("000000", mobile,template, content);
    }
    private boolean isUserStatus(String userId,String userType){
    	boolean flog=false;
    	Map<String, Object> map=new HashMap<String, Object>();
    	map.put("userId", userId);
    	map.put("userType", userType);
    	flog=orderQueryService.isUserStatus(map);
    	return flog;
    }
}
