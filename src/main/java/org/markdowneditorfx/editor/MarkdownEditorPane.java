/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.markdowneditorfx.editor;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ToolBar;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.Nodes;
import org.markdowneditorfx.Messages;
import org.markdowneditorfx.controls.BottomSlidePane;
import org.markdowneditorfx.editor.FindReplacePane.HitsChangeListener;
import org.markdowneditorfx.editor.MarkdownSyntaxHighlighter.ExtraStyledRanges;
import org.markdowneditorfx.util.Action;
import org.markdowneditorfx.util.ActionUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.BOLD;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.CODE;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.FILE_CODE_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.HEADER;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.ITALIC;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.LINK;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.LIST_OL;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.LIST_UL;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PICTURE_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.QUOTE_LEFT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.REPEAT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.RETWEET;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.SEARCH;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.STRIKETHROUGH;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.UNDO;
import static java.util.Collections.singletonList;
import static javafx.scene.input.KeyCode.DIGIT0;
import static javafx.scene.input.KeyCode.MINUS;
import static javafx.scene.input.KeyCode.PLUS;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

/**
 * Markdown editor pane.
 * <p>
 * Uses <a href="https://github.com/vsch/flexmark-java">flexmark-java</a> for parsing markdown.
 * </p>
 *
 * @author Karl Tauber
 */
public class MarkdownEditorPane {

	private static final int DEFAULT_FONT_SIZE = 14;

	private final BorderPane root;
	private final BottomSlidePane searchablePane;
	private final MarkdownTextArea textArea;
	private ContextMenu contextMenu;
	private final SmartEdit smartEdit;
	private final FindReplacePane findReplacePane;
	private final HitsChangeListener findHitsChangeListener;
	private Parser parser;
	private String lineSeparator = getLineSeparatorOrDefault();
	private ToolBar toolBar;

	private final SimpleIntegerProperty fontSizeProperty = new SimpleIntegerProperty(DEFAULT_FONT_SIZE);

	public MarkdownEditorPane() {
		textArea = new MarkdownTextArea();
		textArea.setWrapText(true);
		textArea.setUseInitialStyleForInsertion(true);
		textArea.getStyleClass().add("markdown-editor");
		textArea.getStylesheets().add("application.css");
		textArea.getStylesheets().add("editor.css");
		textArea.getStylesheets().add("prism.css");

		textArea.textProperty().addListener((observable, oldText, newText) -> {
			textChanged(newText);
			hideContextMenu();
		});

		textArea.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, this::showContextMenu);
		textArea.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> hideContextMenu());
		textArea.focusedProperty().addListener(e -> hideContextMenu());

		smartEdit = new SmartEdit(this, textArea);

		// create scroll pane
		VirtualizedScrollPane<MarkdownTextArea> scrollPane = new VirtualizedScrollPane<>(textArea);

		// create border pane
		searchablePane = new BottomSlidePane(scrollPane);

		// create root pane
		root = new BorderPane();
		root.setCenter(searchablePane);
		root.getStylesheets().add("application.css");

		// configure text area graphics
		var overlayGraphicFactory = new ParagraphOverlayGraphicFactory(textArea);
		textArea.setParagraphGraphicFactory(overlayGraphicFactory);
		updateFont();

		// initialize properties
		markdownText.set("");
		markdownAST.set(parseMarkdown(""));

		// find/replace
		findReplacePane = new FindReplacePane(textArea);
		findHitsChangeListener = this::findHitsChanged;
		findReplacePane.addListener(findHitsChangeListener);
		findReplacePane.visibleProperty().addListener((ov, oldVisible, newVisible) -> {
			if (!newVisible) {
				searchablePane.setBottom(null);
			}
		});

		// listen to option changes
		InvalidationListener optionsListener = e -> {
			if (textArea.getScene() == null) {
				return; // editor closed but not yet GCed
			}
			updateFont();
		};
		fontSizeProperty.addListener(optionsListener);

		BooleanProperty canUndo = new SimpleBooleanProperty();
		canUndo.bind(getUndoManager().undoAvailableProperty());
		BooleanProperty canRedo = new SimpleBooleanProperty();
		canRedo.bind(getUndoManager().redoAvailableProperty());

		//region Actions
		Action editUndoAction = new Action(Messages.get("MarkdownEditorPane.editUndoAction"), "Shortcut+Z", UNDO,
			this::undo, canUndo.not());
		Action editRedoAction = new Action(Messages.get("MarkdownEditorPane.editRedoAction"), "Shortcut+Y", REPEAT,
			this::redo, canRedo.not());

		Action insertBoldAction = new Action(Messages.get("MarkdownEditorPane.insertBoldAction"), "Shortcut+B", BOLD,
			() -> smartEdit.insertBold(Messages.get("MarkdownEditorPane.insertBoldText")));
		Action insertItalicAction = new Action(Messages.get("MarkdownEditorPane.insertItalicAction"), "Shortcut+I", ITALIC,
			() -> smartEdit.insertItalic(Messages.get("MarkdownEditorPane.insertItalicText")));
		Action insertStrikethroughAction = new Action(Messages.get("MarkdownEditorPane.insertStrikethroughAction"), "Shortcut+T", STRIKETHROUGH,
			() -> smartEdit.insertStrikethrough(Messages.get("MarkdownEditorPane.insertStrikethroughText")));
		Action insertCodeAction = new Action(Messages.get("MarkdownEditorPane.insertCodeAction"), "Shortcut+K", CODE,
			() -> smartEdit.insertInlineCode(Messages.get("MarkdownEditorPane.insertCodeText")));

		Action insertLinkAction = new Action(Messages.get("MarkdownEditorPane.insertLinkAction"), "Shortcut+L", LINK,
			smartEdit::insertLink);
		Action insertImageAction = new Action(Messages.get("MarkdownEditorPane.insertImageAction"), "Shortcut+G", PICTURE_ALT,
			smartEdit::insertImage);

		Action insertUnorderedListAction = new Action(Messages.get("MarkdownEditorPane.insertUnorderedListAction"), "Shortcut+U", LIST_UL,
			smartEdit::insertUnorderedList);
		Action insertOrderedListAction = new Action(Messages.get("MarkdownEditorPane.insertOrderedListAction"), "Shortcut+Shift+U", LIST_OL,
			() -> smartEdit.surroundSelection("\n\n1. ", ""));
		Action insertBlockquoteAction = new Action(Messages.get("MarkdownEditorPane.insertBlockquoteAction"), "Ctrl+Q", QUOTE_LEFT, // not Shortcut+Q because of conflict on Mac
			() -> smartEdit.surroundSelection("\n\n> ", ""));
		Action insertFencedCodeBlockAction = new Action(Messages.get("MarkdownEditorPane.insertFencedCodeBlockAction"), "Shortcut+Shift+K", FILE_CODE_ALT,
			() -> smartEdit.surroundSelection("\n\n```\n", "\n```\n\n", Messages.get("MarkdownEditorPane.insertFencedCodeBlockText")));

		Action insertHeader1Action = new Action(Messages.get("MarkdownEditorPane.insertHeader1Action"), "Shortcut+1", HEADER,
			() -> smartEdit.insertHeading(1, Messages.get("MarkdownEditorPane.insertHeader1Text")));

		Action editFindAction = new Action(Messages.get("MarkdownEditorPane.editFindAction"), "Shortcut+F", SEARCH,
			() -> find(false));
		Action editReplaceAction = new Action(Messages.get("MarkdownEditorPane.editReplaceAction"), "Shortcut+H", RETWEET,
			() -> find(true));
		Action editFindNextAction = new Action(Messages.get("MarkdownEditorPane.editFindNextAction"), "F3", null,
			() -> findNextPrevious(true));
		Action editFindPreviousAction = new Action(Messages.get("MarkdownEditorPane.editFindPreviousAction"), "Shift+F3", null,
			() -> findNextPrevious(false));
		//endregion Actions

		//region Toolbar
		toolBar = ActionUtils.createToolBar(
			editUndoAction,
			editRedoAction,
			null,
			editFindAction,
			editReplaceAction,
			null,
			new Action(insertBoldAction, smartEdit.boldProperty()),
			new Action(insertItalicAction, smartEdit.italicProperty()),
			new Action(insertStrikethroughAction, smartEdit.strikethroughProperty()),
			new Action(insertCodeAction, smartEdit.codeProperty()),
			null,
			new Action(insertLinkAction, smartEdit.linkProperty()),
			new Action(insertImageAction, smartEdit.imageProperty()),
			null,
			new Action(insertUnorderedListAction, smartEdit.unorderedListProperty()),
			new Action(insertOrderedListAction, smartEdit.orderedListProperty()),
			new Action(insertBlockquoteAction, smartEdit.blockquoteProperty()),
			new Action(insertFencedCodeBlockAction, smartEdit.fencedCodeProperty()),
			null,
			new Action(insertHeader1Action, smartEdit.headerProperty())
		);
		root.setTop(toolBar);
		//endregion Toolbar

		//region Shortcuts
		Nodes.addInputMap(textArea, sequence(
			consume(keyPressed(editFindAction.accelerator), e -> editFindAction.action.call()),
			consume(keyPressed(editReplaceAction.accelerator), e -> editReplaceAction.action.call()),
			consume(keyPressed(editFindNextAction.accelerator), e -> editFindNextAction.action.call()),
			consume(keyPressed(editFindPreviousAction.accelerator), e -> editFindPreviousAction.action.call()),

			consume(keyPressed(PLUS, SHORTCUT_DOWN), this::increaseFontSize),
			consume(keyPressed(MINUS, SHORTCUT_DOWN), this::decreaseFontSize),
			consume(keyPressed(DIGIT0, SHORTCUT_DOWN), this::resetFontSize),

			consume(keyPressed(insertBoldAction.accelerator), e -> insertBoldAction.action.call()),
			consume(keyPressed(insertItalicAction.accelerator), e -> insertItalicAction.action.call()),
			consume(keyPressed(insertStrikethroughAction.accelerator), e -> insertStrikethroughAction.action.call()),
			consume(keyPressed(insertCodeAction.accelerator), e -> insertCodeAction.action.call()),

			consume(keyPressed(insertLinkAction.accelerator), e -> insertLinkAction.action.call()),
			consume(keyPressed(insertImageAction.accelerator), e -> insertImageAction.action.call()),

			consume(keyPressed(insertUnorderedListAction.accelerator), e -> insertUnorderedListAction.action.call()),
			consume(keyPressed(insertOrderedListAction.accelerator), e -> insertOrderedListAction.action.call()),
			consume(keyPressed(insertBlockquoteAction.accelerator), e -> insertBlockquoteAction.action.call()),
			consume(keyPressed(insertFencedCodeBlockAction.accelerator), e -> insertFencedCodeBlockAction.action.call()),

			consume(keyPressed(insertHeader1Action.accelerator), e -> insertHeader1Action.action.call())
		));
		//endregion Shortcuts

		// workaround a problem with wrong selection after undo:
		//   after undo the selection is 0-0, anchor is 0, but caret position is correct
		//   --> set selection to caret position
		textArea.selectionProperty().addListener((observable, oldSelection, newSelection) -> {
			// use runLater because the wrong selection temporary occurs while edition
			Platform.runLater(() -> {
				IndexRange selection = textArea.getSelection();
				int caretPosition = textArea.getCaretPosition();
				if (selection.getStart() == 0 && selection.getEnd() == 0 && textArea.getAnchor() == 0 && caretPosition > 0) {
					textArea.selectRange(caretPosition, caretPosition);
				}
			});
		});
	}

	private void updateFont() {
		textArea.setStyle("-fx-font-size: " + fontSizeProperty.get() + "px;");
	}

	public javafx.scene.Node getNode() {
		return root;
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public boolean isReadOnly() {
		return textArea.isDisable();
	}

	public void setReadOnly(boolean readOnly) {
		textArea.setDisable(readOnly);
	}

	public BooleanProperty readOnlyProperty() {
		return textArea.disableProperty();
	}

	public UndoManager<?> getUndoManager() {
		return textArea.getUndoManager();
	}

	public SmartEdit getSmartEdit() {
		return smartEdit;
	}

	public void requestFocus() {
		Platform.runLater(() -> {
			if (textArea.getScene() != null) {
				textArea.requestFocus();
			} else {
				// text area still does not have a scene
				// --> use listener on scene to make sure that text area receives focus
				ChangeListener<Scene> l = new ChangeListener<>() {
					@Override
					public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
						textArea.sceneProperty().removeListener(this);
						textArea.requestFocus();
					}
				};
				textArea.sceneProperty().addListener(l);
			}
		});
	}

	private String getLineSeparatorOrDefault() {
		return System.getProperty("line.separator", "\n");
	}

	private String determineLineSeparator(String str) {
		int strLength = str.length();
		for (int i = 0; i < strLength; i++) {
			char ch = str.charAt(i);
			if (ch == '\n') {
				return (i > 0 && str.charAt(i - 1) == '\r') ? "\r\n" : "\n";
			}
		}
		return getLineSeparatorOrDefault();
	}

	// 'markdown' property
	public String getMarkdown() {
		String markdown = textArea.getText();
		if (!lineSeparator.equals("\n")) {
			markdown = markdown.replace("\n", lineSeparator);
		}
		return markdown;
	}

	public void setMarkdown(String markdown) {
		// remember old selection range
		IndexRange oldSelection = textArea.getSelection();

		// replace text
		lineSeparator = determineLineSeparator(markdown);
		textArea.replaceText(markdown);

		// restore old selection range
		int newLength = textArea.getLength();
		textArea.selectRange(Math.min(oldSelection.getStart(), newLength), Math.min(oldSelection.getEnd(), newLength));

		// make sure that caret is visible
		textArea.requestFollowCaret();
	}

	//region Properties

	public ObservableValue<String> markdownProperty() {
		return textArea.textProperty();
	}

	// 'markdownText' property
	private final ReadOnlyStringWrapper markdownText = new ReadOnlyStringWrapper();

	public String getMarkdownText() {
		return markdownText.get();
	}

	public ReadOnlyStringProperty markdownTextProperty() {
		return markdownText.getReadOnlyProperty();
	}

	// 'markdownAST' property
	private final ReadOnlyObjectWrapper<Node> markdownAST = new ReadOnlyObjectWrapper<>();

	public Node getMarkdownAST() {
		return markdownAST.get();
	}

	public ReadOnlyObjectProperty<Node> markdownASTProperty() {
		return markdownAST.getReadOnlyProperty();
	}

	// 'selection' property
	public ObservableValue<IndexRange> selectionProperty() {
		return textArea.selectionProperty();
	}

	// 'scrollY' property
	public double getScrollY() {
		return textArea.scrollY.getValue();
	}

	public ObservableValue<Double> scrollYProperty() {
		return textArea.scrollY;
	}

	// 'path' property
	private final ObjectProperty<Path> path = new SimpleObjectProperty<>();

	public Path getPath() {
		return path.get();
	}

	public void setPath(Path path) {
		this.path.set(path);
	}

	public ObjectProperty<Path> pathProperty() {
		return path;
	}

	// 'visible' property
	private final ReadOnlyBooleanWrapper visible = new ReadOnlyBooleanWrapper(false);

	public boolean isVisible() {
		return visible.get();
	}

	public void setVisible(boolean visible) {
		this.visible.set(visible);
	}

	public ReadOnlyBooleanProperty visibleProperty() {
		return visible.getReadOnlyProperty();
	}

	//endregion Properties

	Path getParentPath() {
		Path path = getPath();
		return (path != null) ? path.getParent() : null;
	}

	private void textChanged(String newText) {
		if (searchablePane.getBottom() != null) {
			findReplacePane.removeListener(findHitsChangeListener);
			findReplacePane.textChanged();
			findReplacePane.addListener(findHitsChangeListener);
		}

		if (isReadOnly()) {
			newText = "";
		}

		Node astRoot = parseMarkdown(newText);

//		if (Options.isShowImagesEmbedded())
//		EmbeddedImage.replaceImageSegments(textArea, astRoot, getParentPath());

		applyHighlighting(astRoot);

		markdownText.set(newText);
		markdownAST.set(astRoot);
	}

	private void findHitsChanged() {
		applyHighlighting(markdownAST.get());
	}

	Node parseMarkdown(String text) {
		if (parser == null) {
			parser = Parser.builder().extensions(
				List.of(
					com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension.create(),
					com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension.create(),
					com.vladsch.flexmark.ext.aside.AsideExtension.create(),
					com.vladsch.flexmark.ext.autolink.AutolinkExtension.create(),
					com.vladsch.flexmark.ext.definition.DefinitionExtension.create(),
					com.vladsch.flexmark.ext.footnotes.FootnoteExtension.create(),
					com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create(),
					com.vladsch.flexmark.ext.tables.TablesExtension.create(),
					com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create(),
					com.vladsch.flexmark.ext.toc.TocExtension.create(),
					com.vladsch.flexmark.ext.wikilink.WikiLinkExtension.create(),
					com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension.create()
				)
			).build();
		}
		return parser.parse(text);
	}

	private void applyHighlighting(Node astRoot) {
		List<ExtraStyledRanges> extraStyledRanges = findReplacePane.hasHits()
			? Arrays.asList(
			new ExtraStyledRanges("hit", findReplacePane.getHits()),
			new ExtraStyledRanges("hit-active", singletonList(findReplacePane.getActiveHit()))
		)
			: null;

		MarkdownSyntaxHighlighter.highlight(textArea, astRoot, extraStyledRanges);
	}

	private void increaseFontSize(KeyEvent e) {
		fontSizeProperty.set(fontSizeProperty.getValue() + 1);
	}

	private void decreaseFontSize(KeyEvent e) {
		fontSizeProperty.set(fontSizeProperty.getValue() - 1);
	}

	private void resetFontSize(KeyEvent e) {
		fontSizeProperty.set(DEFAULT_FONT_SIZE);
	}

	public void undo() {
		textArea.getUndoManager().undo();
	}

	public void redo() {
		textArea.getUndoManager().redo();
	}

	public void cut() {
		textArea.cut();
	}

	public void copy() {
		textArea.copy();
	}

	public void paste() {
		textArea.paste();
	}

	public void selectAll() {
		textArea.selectAll();
	}

	public void selectRange(int anchor, int caretPosition) {
		SmartEdit.selectRange(textArea, anchor, caretPosition);
	}

	public void scrollCaretToVisible() {
		scrollParagraphToVisible(textArea.getCurrentParagraph());
	}

	public void scrollParagraphToVisible(int paragraph) {
		try {
			int firstVisible = textArea.firstVisibleParToAllParIndex();
			int lastVisible = textArea.lastVisibleParToAllParIndex();
			int visibleCount = lastVisible - firstVisible;
			int distance = visibleCount / 8;

			if (paragraph > lastVisible - distance) {
				// scroll down so that paragraph is in the upper area
				textArea.showParagraphAtTop(paragraph - distance);

			} else if (paragraph < firstVisible + distance) {
				// scroll up so that paragraph is in the lower area
				textArea.showParagraphAtBottom(paragraph + distance);
			}
		} catch (AssertionError e) {
			// may be thrown in textArea.visibleParToAllParIndex()
			// occurs if the last line is empty and and the text fits into
			// the visible area (no vertical scroll bar shown)
			// --> ignore
		}
	}

	//---- context menu -------------------------------------------------------

	private void showContextMenu(ContextMenuEvent e) {
		if (e.isConsumed()) {
			return;
		}

		// hide old context menu
		hideContextMenu();

		// determine character index and menu x/y
		int characterIndex;
		double menuX = e.getScreenX();
		double menuY = e.getScreenY();
		if (e.isKeyboardTrigger()) {
			// keyboard triggered --> use caret
			characterIndex = textArea.getCaretPosition();

			Optional<Bounds> caretBounds = textArea.getCaretBounds();
			if (caretBounds.isPresent()) {
				menuX = caretBounds.get().getMaxX();
				menuY = caretBounds.get().getMaxY();
			}
		} else {
			// mouse triggered
			CharacterHit hit = textArea.hit(e.getX(), e.getY());
			characterIndex = hit.getCharacterIndex().orElse(-1);
		}

		// create context menu
		ContextMenu contextMenu = new ContextMenu();

		// initialize context menu
		SmartEditActions.initContextMenu(this, contextMenu, characterIndex);

		if (contextMenu.getItems().isEmpty()) {
			return;
		}

		// show context menu
		contextMenu.show(textArea, menuX, menuY);
		this.contextMenu = contextMenu;
		e.consume();
	}

	public void hideContextMenu() {
		if (contextMenu != null) {
			contextMenu.hide();
			contextMenu = null;
		}
	}

	//---- find/replace -------------------------------------------------------

	public void find(boolean replace) {
		if (searchablePane.getBottom() == null) {
			searchablePane.setBottom(findReplacePane.getNode());
		}

		findReplacePane.show(replace, true);
	}

	public void findNextPrevious(boolean next) {
		if (searchablePane.getBottom() == null) {
			// show pane
			find(false);
			return;
		}

		if (next) {
			findReplacePane.findNext();
		} else {
			findReplacePane.findPrevious();
		}
	}

}
