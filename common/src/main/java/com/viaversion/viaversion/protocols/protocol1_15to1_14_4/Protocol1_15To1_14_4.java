/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_15to1_14_4;

import com.viaversion.viaversion.api.data.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriters.MetadataRewriter;
import com.viaversion.viaversion.api.rewriters.RegistryType;
import com.viaversion.viaversion.api.rewriters.SoundRewriter;
import com.viaversion.viaversion.api.rewriters.StatisticsRewriter;
import com.viaversion.viaversion.api.rewriters.TagRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.metadata.MetadataRewriter1_15To1_14_4;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.packets.PlayerPackets;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.storage.EntityTracker1_15;

public class Protocol1_15To1_14_4 extends Protocol<ClientboundPackets1_14, ClientboundPackets1_15, ServerboundPackets1_14, ServerboundPackets1_14> {

    public static final MappingData MAPPINGS = new MappingData();
    private TagRewriter tagRewriter;

    public Protocol1_15To1_14_4() {
        super(ClientboundPackets1_14.class, ClientboundPackets1_15.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        MetadataRewriter metadataRewriter = new MetadataRewriter1_15To1_14_4(this);

        EntityPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_14.ENTITY_SOUND); // Entity Sound Effect (added somewhere in 1.14)
        soundRewriter.registerSound(ClientboundPackets1_14.SOUND);

        new StatisticsRewriter(this, metadataRewriter::getNewEntityId).register(ClientboundPackets1_14.STATISTICS);

        registerIncoming(ServerboundPackets1_14.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        tagRewriter = new TagRewriter(this, EntityPackets::getNewEntityId);
        tagRewriter.register(ClientboundPackets1_14.TAGS, RegistryType.ENTITY);
    }

    @Override
    protected void onMappingDataLoaded() {
        int[] shulkerBoxes = new int[17];
        int shulkerBoxOffset = 501;
        for (int i = 0; i < 17; i++) {
            shulkerBoxes[i] = shulkerBoxOffset + i;
        }
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:shulker_boxes", shulkerBoxes);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_15(userConnection));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }
}