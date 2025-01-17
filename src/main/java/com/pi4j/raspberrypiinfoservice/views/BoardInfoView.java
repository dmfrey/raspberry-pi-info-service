package com.pi4j.raspberrypiinfoservice.views;

import com.pi4j.raspberrypiinfo.definition.BoardModel;
import com.pi4j.raspberrypiinfoservice.views.header.HeaderLegend;
import com.pi4j.raspberrypiinfoservice.views.header.HeaderPinView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

@PageTitle("Raspberry Pi Board Information")
@Route(value = "board-information", layout = BaseLayout.class)
@RouteAlias(value = "", layout = BaseLayout.class)
public class BoardInfoView extends VerticalLayout {

    private static final Logger logger = LogManager.getLogger(BoardInfoView.class);

    private final VerticalLayout holder = new VerticalLayout();
    private final ListBox<BoardModel> listBox = new ListBox<>();

    public BoardInfoView() {
        setSpacing(false);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);

        listBox.addValueChangeListener(e -> showBoard(e.getValue()));
        listBox.setMinWidth(250, Unit.PIXELS);
        listBox.setHeightFull();
        listBox.setRenderer(new ComponentRenderer<>(board -> {
            var lbl = new Label(board.getLabel());
            lbl.setWidthFull();
            return lbl;
        }));

        holder.setPadding(true);
        holder.setMargin(false);
        holder.setSpacing(true);

        var split = new SplitLayout(listBox, holder);
        split.setHeightFull();
        split.setWidthFull();

        add(split);
    }

    @Override
    public void onAttach(AttachEvent event) {
        UI.getCurrent().access(() -> listBox.setItems(Arrays.stream(BoardModel.values())
                .sorted(Comparator.comparing(BoardModel::getLabel))
                .toList()));
    }

    private void showBoard(BoardModel boardModel) {
        holder.removeAll();

        if (boardModel == null) {
            return;
        }

        logger.info("Board selected: {}", boardModel.name());

        // Use access to prevent long-running load board on top of the screen
        UI.getCurrent().access(() -> {
            holder.add(new H2(boardModel.getLabel()));

            var img = new Image("/boards/" + boardModel.name() + ".jpg", boardModel.getLabel());
            img.setHeight(200, Unit.PIXELS);
            holder.add(img);

            holder.add(new H3("Board info"));

            holder.add(getLabelValue("Board type", boardModel.getBoardType().name()));
            holder.add(getLabelValue("Released", boardModel.getReleaseDate().getMonth().getDisplayName(TextStyle.FULL, Locale.UK))
                    + " " + boardModel.getReleaseDate().getYear());
            holder.add(getLabelValue("Model", boardModel.getModel().name()));
            holder.add(getLabelValue("Header version", boardModel.getHeaderVersion().getLabel()));
            holder.add(getLabelValue("Release date", boardModel.getReleaseDate().toString()));
            holder.add(getLabelValue("SOC", boardModel.getSoc().name()
                    + " / " + boardModel.getSoc().getInstructionSet().getLabel()));
            holder.add(getLabelValue("CPU", boardModel.getNumberOfCpu()
                    + "x " + boardModel.getCpu().getLabel()
                    + " @ " + (boardModel.getVersionsProcessorSpeedInMhz().isEmpty() ? "" :
                    boardModel.getVersionsProcessorSpeedInMhz().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "))) + "Mhz"));
            holder.add(getLabelValue("Memory in GB", boardModel.getVersionsMemoryInGb().isEmpty() ? "" :
                    boardModel.getVersionsMemoryInGb().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "))));
            holder.add(getLabelValue("Remarks", boardModel.getRemarks().isEmpty() ? "" :
                    String.join(", ", boardModel.getRemarks())));

            holder.add(new H3("Header(s)"));

            if (boardModel.getHeaderVersion().getHeaderPins() != null
                    && !boardModel.getHeaderVersion().getHeaderPins().isEmpty()) {
                boardModel.getHeaderVersion().getHeaderPins().forEach(hp -> {
                    holder.add(new H4(hp.getLabel()));
                    holder.add(new HeaderPinView(hp));
                });
                holder.add(new HeaderLegend());
            }
        });
    }

    private HorizontalLayout getLabelValue(String label, String value) {
        var lbl = new Label(label);
        lbl.setWidth(250, Unit.PIXELS);
        var labelValueHolder = new HorizontalLayout(lbl, new Label(value));
        labelValueHolder.setMargin(false);
        labelValueHolder.setPadding(false);
        return labelValueHolder;
    }
}
