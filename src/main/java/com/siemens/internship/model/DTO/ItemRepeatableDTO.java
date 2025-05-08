package com.siemens.internship.model.DTO;

import com.siemens.internship.model.DAO.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRepeatableDTO {
    private ItemDTO item;
    private int count;
}
