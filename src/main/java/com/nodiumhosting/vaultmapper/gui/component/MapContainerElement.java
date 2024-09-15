package com.nodiumhosting.vaultmapper.gui.component;

import com.nodiumhosting.vaultmapper.map.CellType;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.snapshots.MapSnapshot;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IMutablePosition;
import iskallia.vault.client.gui.framework.spatial.spi.IMutableSpatial;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;

import java.util.*;

import net.minecraft.network.chat.TextComponent;

public class MapContainerElement extends VerticalScrollClipContainer<MapContainerElement> {
    public MapContainerElement(ISpatial spatial, UUID vaultUuid) {
        super(spatial, Padding.of(2, 0));
        this.addElement(new MapElement(Spatials.positionY(3), vaultUuid)).postLayout((screen, gui, parent, world) -> {
            world.translateX((this.innerWidth() - world.width()) / 2);
            return true;
        });
    }

    private static final class MapElement extends ElasticContainerElement<MapElement> {
        private MapElement(IPosition position, UUID vaultUuid) {
            super(Spatials.positionXYZ(position));
            Optional<MapSnapshot> optMap = MapSnapshot.from(vaultUuid);
            if (optMap.isEmpty()) {
                return;
            }
            MapSnapshot map = optMap.get();

            List<VaultCell> cells = map.cells;
            int cellCount = cells.stream().filter(cell -> cell.cellType == CellType.ROOM && cell.explored).toArray().length;
            int inscriptionCount = cells.stream().filter(cell -> cell.inscripted).toArray().length;
            int markedCount = cells.stream().filter(cell -> cell.marked).toArray().length;

            IMutableSpatial spatial = Spatials.positionXYZ(position);
            spatial.positionZ(10); // TRY JUST ONE

            this.addElement(new LabelElement(spatial.positionX(-35).positionY(5), new TextComponent("Explored Rooms: " + cellCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-35).positionY(15), new TextComponent("Inscription Rooms: " + inscriptionCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-35).positionY(25), new TextComponent("Marked Rooms: " + markedCount), new LabelTextStyle.Builder()));

            List.of(-40, -50, -55, -60).forEach(e -> {
                this.addElement(new LabelElement(spatial.positionX(e).positionY(Math.abs(e)), new TextComponent(e.toString()), new LabelTextStyle.Builder()));
            });
        }
    }
}
