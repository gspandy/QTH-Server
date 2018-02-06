package com.nowbook.rlt.presale;

import com.nowbook.rlt.BaseDaoTest;
import com.nowbook.rlt.presale.dao.AddressStorageDao;
import com.nowbook.rlt.presale.model.AddressStorage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AddressStorageDaoTest extends BaseDaoTest {

    @Autowired
    private AddressStorageDao addressStorageDao;

    private AddressStorage addressStorage;

    @Before
    public void setUp() throws Exception {
        addressStorage = new AddressStorage();
        addressStorage.setItemId(11L);
        addressStorage.setAddressId(22);
        addressStorage.setStorageId(33L);

        addressStorageDao.create(addressStorage);

    }

    @Test
    public void testFindByItemIdAndAddressId() throws Exception{
        AddressStorage actual = addressStorageDao.findByItemIdAndAddressId(addressStorage.getItemId(), addressStorage.getAddressId());

        assertThat(actual , is(addressStorage));
    }

    @Test
    public void changeStorageIdByItemIdAndAddressId() throws Exception{
        boolean success = addressStorageDao.changeStorageIdByItemIdAndAddressId(addressStorage.getItemId(), addressStorage.getAddressId(),100L);
        assertThat(success, is(true));

        AddressStorage actual = addressStorageDao.findByItemIdAndAddressId(addressStorage.getItemId(), addressStorage.getAddressId());
        assertThat(actual.getStorageId(), is(100L));
    }
}