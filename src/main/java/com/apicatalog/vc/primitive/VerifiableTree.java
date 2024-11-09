package com.apicatalog.vc.primitive;

import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.builder.GenericTreeCompiler;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.primitive.GenericContainer;
import com.apicatalog.linkedtree.traversal.NodePointer;
import com.apicatalog.linkedtree.traversal.TreeComposer;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.VcdmVocab;

@Deprecated
public class VerifiableTree {

    public static LinkedTree unsigned(Verifiable verifiable) {

        if (verifiable.isCredential()) {
            return ((Linkable)verifiable).ld().asFragment().root();
        }

        if (verifiable.isPresentation()) {

            var x = new TreeComposer(((Linkable)verifiable).ld().asFragment().root()) {

                GenericTreeCompiler compiler = new GenericTreeCompiler();

                @Override
                protected void end(LinkedNode node, Object[] path) {
                    compiler.accept(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);
                }

                @Override
                protected void begin(LinkedNode node, Object[] path) throws TreeBuilderError {
                    compiler.test(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);

                }
            };
            x.inject(NodePointer.of(0, VcdmVocab.VERIFIABLE_CREDENTIALS.uri()),
                    GenericContainer.empty(null)); // FIXME hack

            int index = 0;
            for (Credential credential : verifiable.asPresentation().credentials()) {
                x.inject(NodePointer.of(
                        0,
                        VcdmVocab.VERIFIABLE_CREDENTIALS.uri(),
                        index),
                        ((Linkable)credential).ld().asFragment().root())
                        .inject(NodePointer.of(0, VcdmVocab.VERIFIABLE_CREDENTIALS.uri(), index,
                                VcdmVocab.PROOF.uri()),
                                GenericContainer.empty(null)); // FIXME hack

                int proofIndex = 0;
                for (Proof proof : credential.proofs()) {
                    x.inject(NodePointer.of(0,
                            VcdmVocab.VERIFIABLE_CREDENTIALS.uri(),
                            index,
                            VcdmVocab.PROOF.uri(),
                            proofIndex++),
                            ((Linkable)proof).ld().asFragment().root());
                }
                index++;
            }

            try {
                x.compose();
                return x.compiler.tree();
            } catch (TreeBuilderError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static LinkedTree compose(Verifiable verifiable) {
        if (verifiable.isCredential()) {
            var crendential = new TreeComposer(((Linkable)verifiable).ld().asFragment().root()) {

                GenericTreeCompiler compiler = new GenericTreeCompiler();

                @Override
                protected void end(LinkedNode node, Object[] path) {
                    compiler.accept(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);
                }

                @Override
                protected void begin(LinkedNode node, Object[] path) throws TreeBuilderError {
                    compiler.test(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);

                }
            };

            if (!verifiable.proofs().isEmpty()) {
                crendential.inject(NodePointer.of(0,
                        VcdmVocab.PROOF.uri()),
                        GenericContainer.empty(null)); // FIXME hack

                int proofIndex = 0;
                for (Proof proof : verifiable.proofs()) {
                    crendential.inject(NodePointer.of(0,
                            VcdmVocab.PROOF.uri(),
                            proofIndex++),
                            ((Linkable)proof).ld().asFragment().root());
                }
            }

            try {
                crendential.compose();
                return crendential.compiler.tree();
            } catch (TreeBuilderError e) {
                e.printStackTrace();    //FIXME
            }
        }

        if (verifiable.isPresentation()) {

            var presentation = new TreeComposer(((Linkable)verifiable).ld().asFragment().root()) {

                GenericTreeCompiler compiler = new GenericTreeCompiler();

                @Override
                protected void end(LinkedNode node, Object[] path) {
                    compiler.accept(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);
                }

                @Override
                protected void begin(LinkedNode node, Object[] path) throws TreeBuilderError {
                    compiler.test(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);

                }
            };

            if (!verifiable.proofs().isEmpty()) {
                presentation.inject(NodePointer.of(0,
                        VcdmVocab.PROOF.uri()),
                        GenericContainer.empty(null)); // FIXME hack

                int proofIndex = 0;
                for (Proof proof : verifiable.proofs()) {
                    presentation.inject(NodePointer.of(0,
                            VcdmVocab.PROOF.uri(),
                            proofIndex++),
                            ((Linkable)proof).ld().asFragment().root());
                }
            }

            try {
                presentation.compose();
                return presentation.compiler.tree();
            } catch (TreeBuilderError e) {
                e.printStackTrace();
            }
        }
        return null;
        
    }
    
    public static LinkedTree complete(Verifiable verifiable) {

        if (verifiable.isCredential()) {
            var crendential = new TreeComposer(((Linkable)verifiable).ld().asFragment().root()) {

                GenericTreeCompiler compiler = new GenericTreeCompiler();

                @Override
                protected void end(LinkedNode node, Object[] path) {
                    compiler.accept(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);
                }

                @Override
                protected void begin(LinkedNode node, Object[] path) throws TreeBuilderError {
                    compiler.test(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);

                }
            };

            if (!verifiable.proofs().isEmpty()) {
                crendential.inject(NodePointer.of(0,
                        VcdmVocab.PROOF.uri()),
                        GenericContainer.empty(null)); // FIXME hack

                int proofIndex = 0;
                for (Proof proof : verifiable.proofs()) {
                    crendential.inject(NodePointer.of(0,
                            VcdmVocab.PROOF.uri(),
                            proofIndex++),
                            ((Linkable)proof).ld().asFragment().root());
                }
            }

            try {
                crendential.compose();
                return crendential.compiler.tree();
            } catch (TreeBuilderError e) {
                e.printStackTrace();    //FIXME
            }
        }

        if (verifiable.isPresentation()) {

            var presentation = new TreeComposer(((Linkable)verifiable).ld().asFragment().root()) {

                GenericTreeCompiler compiler = new GenericTreeCompiler();

                @Override
                protected void end(LinkedNode node, Object[] path) {
                    compiler.accept(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);
                }

                @Override
                protected void begin(LinkedNode node, Object[] path) throws TreeBuilderError {
                    compiler.test(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);

                }
            };
            presentation.inject(NodePointer.of(0, VcdmVocab.VERIFIABLE_CREDENTIALS.uri()),
                    GenericContainer.empty(null)); // FIXME hack

            int index = 0;
            for (Credential credential : verifiable.asPresentation().credentials()) {
                presentation.inject(NodePointer.of(
                        0,
                        VcdmVocab.VERIFIABLE_CREDENTIALS.uri(),
                        index),
                        ((Linkable)credential).ld().asFragment().root());

                if (!credential.proofs().isEmpty()) {
                    presentation.inject(NodePointer.of(0, VcdmVocab.VERIFIABLE_CREDENTIALS.uri(), index,
                            VcdmVocab.PROOF.uri()),
                            GenericContainer.empty(null)); // FIXME hack

                    int proofIndex = 0;
                    for (Proof proof : credential.proofs()) {
                        presentation.inject(NodePointer.of(0,
                                VcdmVocab.VERIFIABLE_CREDENTIALS.uri(),
                                index,
                                VcdmVocab.PROOF.uri(),
                                proofIndex++),
                                ((Linkable)proof).ld().asFragment().root());
                    }
                }
                index++;
            }

            if (!verifiable.proofs().isEmpty()) {
                presentation.inject(NodePointer.of(0,
                        VcdmVocab.PROOF.uri()),
                        GenericContainer.empty(null)); // FIXME hack

                int proofIndex = 0;
                for (Proof proof : verifiable.proofs()) {
                    presentation.inject(NodePointer.of(0,
                            VcdmVocab.PROOF.uri(),
                            proofIndex++),
                            ((Linkable)proof).ld().asFragment().root());
                }
            }

            try {
                presentation.compose();
                return presentation.compiler.tree();
            } catch (TreeBuilderError e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
