/**
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.ide.tests.testlanguage.ide.server;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.serializer.IChangeSerializer;
import org.eclipse.xtext.ide.serializer.IEmfResourceChange;
import org.eclipse.xtext.ide.serializer.ITextDocumentChange;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.Member;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.Model;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.TypeDeclaration;
import org.eclipse.xtext.ide.tests.testlanguage.validation.TestLanguageValidator;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.CollectionBasedAcceptor;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class CodeActionService implements ICodeActionService {
  @Inject
  private IChangeSerializer serializer;
  
  @Override
  public List<Either<Command, CodeAction>> getCodeActions(final Document document, final XtextResource resource, final CodeActionParams params, final CancelIndicator indicator) {
    final ArrayList<Either<Command, CodeAction>> actions = CollectionLiterals.<Either<Command, CodeAction>>newArrayList();
    List<Diagnostic> _diagnostics = params.getContext().getDiagnostics();
    for (final Diagnostic d : _diagnostics) {
      String _code = d.getCode();
      if (_code != null) {
        switch (_code) {
          case TestLanguageValidator.INVALID_NAME:
            Either<Command, CodeAction> _forLeft = Either.<Command, CodeAction>forLeft(this.fixInvalidName(d, document, resource, params));
            actions.add(_forLeft);
            break;
          case TestLanguageValidator.UNSORTED_MEMBERS:
            Either<Command, CodeAction> _forRight = Either.<Command, CodeAction>forRight(this.fixUnsortedMembers(d, document, resource, params));
            actions.add(_forRight);
            break;
        }
      }
    }
    return actions;
  }
  
  private Command fixInvalidName(final Diagnostic d, final Document doc, final XtextResource res, final CodeActionParams params) {
    final String string = doc.getSubstring(d.getRange());
    Command _command = new Command();
    final Procedure1<Command> _function = (Command it) -> {
      it.setCommand("my.textedit.command");
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Make \'");
      _builder.append(string);
      _builder.append("\' upper case");
      it.setTitle(_builder.toString());
      WorkspaceEdit _workspaceEdit = new WorkspaceEdit();
      final Procedure1<WorkspaceEdit> _function_1 = (WorkspaceEdit it_1) -> {
        TextEdit _textEdit = new TextEdit();
        final Procedure1<TextEdit> _function_2 = (TextEdit it_2) -> {
          it_2.setNewText(StringExtensions.toFirstUpper(string));
          it_2.setRange(d.getRange());
        };
        TextEdit _doubleArrow = ObjectExtensions.<TextEdit>operator_doubleArrow(_textEdit, _function_2);
        it_1.getChanges().put(params.getTextDocument().getUri(), Collections.<TextEdit>unmodifiableList(CollectionLiterals.<TextEdit>newArrayList(_doubleArrow)));
      };
      WorkspaceEdit _doubleArrow = ObjectExtensions.<WorkspaceEdit>operator_doubleArrow(_workspaceEdit, _function_1);
      it.setArguments(Collections.<Object>unmodifiableList(CollectionLiterals.<Object>newArrayList(_doubleArrow)));
    };
    return ObjectExtensions.<Command>operator_doubleArrow(_command, _function);
  }
  
  private CodeAction fixUnsortedMembers(final Diagnostic d, final Document doc, final XtextResource res, final CodeActionParams params) {
    final IChangeSerializer.IModification<Resource> _function = (Resource copiedResource) -> {
      final Model model = IterableExtensions.<Model>head(Iterables.<Model>filter(copiedResource.getContents(), Model.class));
      EList<TypeDeclaration> _types = model.getTypes();
      for (final TypeDeclaration type : _types) {
        final Comparator<Member> _function_1 = (Member a, Member b) -> {
          String _name = a.getName();
          String _name_1 = b.getName();
          return (_name.compareTo(_name_1));
        };
        ECollections.<Member>sort(type.getMembers(), _function_1);
      }
    };
    final WorkspaceEdit wsEdit = this.recordWorkspaceEdit(doc, res, _function);
    CodeAction _codeAction = new CodeAction();
    final Procedure1<CodeAction> _function_1 = (CodeAction it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Sort Members");
      it.setTitle(_builder.toString());
      it.setDiagnostics(Collections.<Diagnostic>unmodifiableList(CollectionLiterals.<Diagnostic>newArrayList(d)));
      it.setEdit(wsEdit);
    };
    return ObjectExtensions.<CodeAction>operator_doubleArrow(_codeAction, _function_1);
  }
  
  private WorkspaceEdit recordWorkspaceEdit(final Document doc, final XtextResource resource, final IChangeSerializer.IModification<Resource> mod) {
    try {
      final XtextResourceSet rs = new XtextResourceSet();
      final Resource copy = rs.createResource(resource.getURI());
      String _text = resource.getParseResult().getRootNode().getText();
      StringInputStream _stringInputStream = new StringInputStream(_text);
      copy.load(_stringInputStream, CollectionLiterals.<Object, Object>emptyMap());
      this.serializer.<Resource>addModification(copy, mod);
      final ArrayList<IEmfResourceChange> documentchanges = CollectionLiterals.<IEmfResourceChange>newArrayList();
      this.serializer.applyModifications(CollectionBasedAcceptor.<IEmfResourceChange>of(documentchanges));
      WorkspaceEdit _workspaceEdit = new WorkspaceEdit();
      final Procedure1<WorkspaceEdit> _function = (WorkspaceEdit it) -> {
        Iterable<ITextDocumentChange> _filter = Iterables.<ITextDocumentChange>filter(documentchanges, ITextDocumentChange.class);
        for (final ITextDocumentChange documentchange : _filter) {
          {
            final Function1<ITextReplacement, TextEdit> _function_1 = (ITextReplacement replacement) -> {
              TextEdit _textEdit = new TextEdit();
              final Procedure1<TextEdit> _function_2 = (TextEdit it_1) -> {
                it_1.setNewText(replacement.getReplacementText());
                Position _position = doc.getPosition(replacement.getOffset());
                Position _position_1 = doc.getPosition(replacement.getEndOffset());
                Range _range = new Range(_position, _position_1);
                it_1.setRange(_range);
              };
              return ObjectExtensions.<TextEdit>operator_doubleArrow(_textEdit, _function_2);
            };
            final List<TextEdit> edits = ListExtensions.<ITextReplacement, TextEdit>map(documentchange.getReplacements(), _function_1);
            it.getChanges().put(documentchange.getNewURI().toString(), edits);
          }
        }
      };
      return ObjectExtensions.<WorkspaceEdit>operator_doubleArrow(_workspaceEdit, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
