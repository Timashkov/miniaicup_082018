package chipmunk_bind


data class cpBB(var l: Float, var b: Float, var r: Float, var t: Float)

interface IcpSpatialIndexBBFunc {
    fun perform(obj: Any): cpBB
}

interface IcpBBTreeVelocityFunc {
    fun perform(obj: Any?): cpVect
}

class Node {
    var obj: Any? = null
    var bb: cpBB? = null
    var parent: Any? = null

//    union
//    {
//        // Internal nodes
//        struct { Node * a, *b; } children;
//
//        // Leaves
//        struct {
//            cpTimestamp stamp;
//            Pair * pairs;
//        } leaf;
//    } node;
};

interface ICPSpatialIndexIteratorFunc {
    fun perform(obj: Any, data: Any?)
}

/// Spatial query callback function type.
interface ICPSpatialIndexQueryFunc {
    fun perform(obj1: Any?, obj2: Any?, id: Int, data: Any?): Int
}

/// Spatial segment query callback function type.
interface ICPSpatialIndexSegmentQueryFunc {
    fun perform(obj1: Any, obj2: Any, data: Any?): Float
}

val NearestPointQueryNearest = object : ICPSpatialIndexQueryFunc {
    override fun perform(obj1: Any?, obj2: Any?, id: Int, data: Any?): Int {

        val context = obj1 as PointQueryContext
        val shape = obj2 as cpShape
        val out = data as cpPointQueryInfo

        if (!cpShapeFilterReject(shape.filter, context.filter) && !shape.sensor){
            val info = cpPointQueryInfo()

            cpShapePointQuery(shape, context.point, info)

            if (info.distance < out.distance) {
                out.distance = info.distance
                out.gradient = info.gradient
                out.point = info.point
                out.shape = info.shape
            }
        }

        return id;
    }
}

fun cpShapePointQuery(shape: cpShape, p:cpVect, info:cpPointQueryInfo ): Float {

    shape.pointQuery( p, info)
    return info.distance
}

fun cpShapeFilterReject(a: cpShapeFilter, b: cpShapeFilter): Boolean {
    // Reject the collision if:
    return (
            // They are in the same non-zero group.
            (a.group.groupId != 0 && a.group.groupId == b.group.groupId) ||
                    // One of the category/mask combinations fails.
                    (a.categories.mask.and(b.mask.mask)) == 0 ||
                    (b.categories.mask.and(a.mask.mask)) == 0
            )
}


abstract class cpSpatialIndex {
    //typedef void (*cpSpatialIndexDestroyImpl)(cpSpatialIndex *index);
    abstract fun destroy()

    //typedef int (*cpSpatialIndexCountImpl)(cpSpatialIndex *index);
    abstract fun count(): Int

    //typedef void (*cpSpatialIndexEachImpl)(cpSpatialIndex *index, cpSpatialIndexIteratorFunc func, void *data);
    abstract fun each(func: ICPSpatialIndexIteratorFunc, data: Any?)

    //    typedef cpBool (*cpSpatialIndexContainsImpl)(cpSpatialIndex *index, void *obj, cpHashValue hashid);
    abstract fun contains(obj: Any?, hashid: Int): Boolean

    //    typedef void (*cpSpatialIndexInsertImpl)(cpSpatialIndex *index, void *obj, cpHashValue hashid);
    abstract fun insert(obj: Any?, hashid: Int)

    //    typedef void (*cpSpatialIndexRemoveImpl)(cpSpatialIndex *index, void *obj, cpHashValue hashid);
    abstract fun remove(obj: Any?, hashid: Int)

    //    typedef void (*cpSpatialIndexReindexImpl)(cpSpatialIndex *index);
    abstract fun reindex()

    //    typedef void (*cpSpatialIndexReindexObjectImpl)(cpSpatialIndex *index, void *obj, cpHashValue hashid);
    abstract fun reindexObject(obj: Any?, hashid: Int)

    //    typedef void (*cpSpatialIndexReindexQueryImpl)(cpSpatialIndex *index, cpSpatialIndexQueryFunc func, void *data);
    abstract fun reindexQuery(func: ICPSpatialIndexQueryFunc, data: Any?)

    //    typedef void (*cpSpatialIndexQueryImpl)(cpSpatialIndex *index, void *obj, cpBB bb, cpSpatialIndexQueryFunc func, void *data);
    abstract fun query(obj: Any?, bb: cpBB, func: ICPSpatialIndexQueryFunc, data: Any?)

    //    typedef void (*cpSpatialIndexSegmentQueryImpl)(cpSpatialIndex *index, void *obj, cpVect a, cpVect b, cpFloat t_exit, cpSpatialIndexSegmentQueryFunc func, void *data);
    abstract fun segmentQuery(obj: Any?, a: cpVect, b: cpVect, t_exit: Float, func: ICPSpatialIndexSegmentQueryFunc, data: Any?)

    // cpSpatialIndexBBFunc bbfunc;
    var bbfunc: IcpSpatialIndexBBFunc? = null

    //cpSpatialIndex *staticIndex, *dynamicIndex;
    var staticIndex: cpSpatialIndex? = null
    var dynamicIndex: cpSpatialIndex? = null
};

class cpBBTree(bbfunc: IcpSpatialIndexBBFunc?, staticIndex: cpSpatialIndex?) : cpSpatialIndex() {


    init {
        this.bbfunc = bbfunc
        this.staticIndex = staticIndex
        if (this.staticIndex != null) {
            this.staticIndex?.dynamicIndex = this
        }


//        tree->leaves = cpHashSetNew(0, (cpHashSetEqlFunc)leafSetEql);
//        tree->root = NULL;
//
//        tree->pooledNodes = NULL;
//        tree->allocatedBuffers = cpArrayNew(0);

    }

    var velocityFunc: IcpBBTreeVelocityFunc? = null

    var leaves: HashSet<Any> = HashSet()

//    cpHashSet *leaves;
//    Node *root;
//
//    Node *pooledNodes;
//    Pair *pooledPairs;
//    cpArray *allocatedBuffers;

    var stamp: Long = 0L
    override fun destroy() {
//        cpHashSetFree(leaves)
//
//        if (allocatedBuffers) cpArrayFreeEach(allocatedBuffers, cpfree)
//
//        cpArrayFree(allocatedBuffers)
    }

    override fun count(): Int {
        return leaves.size
        //return cpHashSetCount(leaves)
    }

    override fun contains(obj: Any?, hashid: Int): Boolean {
        return leaves.find { it.hashCode() == hashid } != null
//        return (cpHashSetFind(leaves, hashid, obj) != NULL)
    }

    override fun insert(obj: Any?, hashid: Int) {


//        Node * leaf = (Node *) cpHashSetInsert (tree->leaves, hashid, obj, (cpHashSetTransFunc)leafSetTrans, tree);
//
//        Node * root = tree->root;
//        tree->root = SubtreeInsert(root, leaf, tree);
//
//        leaf->STAMP = GetMasterTree(tree)->stamp;
//        LeafAddPairs(leaf, tree);
//        IncrementStamp(tree);
    }

    override fun remove(obj: Any?, hashid: Int) {
//        Node * leaf = (Node *) cpHashSetRemove (tree->leaves, hashid, obj);
//
//        tree->root = SubtreeRemove(tree->root, leaf, tree);
//        PairsClear(leaf, tree);
//        NodeRecycle(tree, leaf);

    }

    override fun reindex() {
        reindexQuery(object : ICPSpatialIndexQueryFunc {
            override fun perform(obj1: Any?, obj2: Any?, id: Int, data: Any?): Int {
                return id
            }
        }, null)
    }

    override fun reindexQuery(func: ICPSpatialIndexQueryFunc, data: Any?) {
//    static void
//    cpBBTreeReindexQuery(cpBBTree *tree, cpSpatialIndexQueryFunc func, void *data)
//    {
//        if(!tree->root) return;
//
//        // LeafUpdate() may modify tree->root. Don't cache it.
//        cpHashSetEach(tree->leaves, (cpHashSetIteratorFunc)LeafUpdateWrap, tree);
//
//        cpSpatialIndex *staticIndex = tree->spatialIndex.staticIndex;
//        Node *staticRoot = (staticIndex && staticIndex->klass == Klass() ? ((cpBBTree *)staticIndex)->root : NULL);
//
//        MarkContext context = {tree, staticRoot, func, data};
//        MarkSubtree(tree->root, &context);
//        if(staticIndex && !staticRoot) cpSpatialIndexCollideStatic((cpSpatialIndex *)tree, staticIndex, func, data);
//
//        IncrementStamp(tree);
//    }

    }

    override fun reindexObject(obj: Any?, hashid: Int) {
///*    static void
//            cpBBTreeReindexObject(cpBBTree *tree, void *obj, cpHashValue hashid)
//            {
//                Node *leaf = (Node *)cpHashSetFind(tree->leaves, hashid, obj);
//                if(leaf){
//                    if(LeafUpdate(leaf, tree)) LeafAddPairs(leaf, tree);
//                    IncrementStamp(tree);
//                }
//            }
//*/

    }

    override fun query(obj: Any?, bb: cpBB, func: ICPSpatialIndexQueryFunc, data: Any?) {
        //        (cpSpatialIndexQueryImpl)cpBBTreeQuery,
//        /*
//    static void
//    cpBBTreeQuery(cpBBTree *tree, void *obj, cpBB bb, cpSpatialIndexQueryFunc func, void *data)
//    {
//        if(tree->root) SubtreeQuery(tree->root, obj, bb, func, data);
//    }*/
    }

    override fun segmentQuery(obj: Any?, a: cpVect, b: cpVect, t_exit: Float, func: ICPSpatialIndexSegmentQueryFunc, data: Any?) {
        //        (cpSpatialIndexSegmentQueryImpl)cpBBTreeSegmentQuery,/*
//	static void
//cpBBTreeSegmentQuery(cpBBTree *tree, void *obj, cpVect a, cpVect b, cpFloat t_exit, cpSpatialIndexSegmentQueryFunc func, void *data)
//{
//	Node *root = tree->root;
//	if(root) SubtreeSegmentQuery(root, obj, a, b, t_exit, func, data);
//}
//*/
    }

    override fun each(func: ICPSpatialIndexIteratorFunc, data: Any?) {

    }

};


fun cpBBNewForExtents(c: cpVect, hw: Float, hh: Float): cpBB {
    return cpBB(c.x - hw, c.y - hh, c.x + hw, c.y + hh)
}

/// Constructs a cpBB for a circle with the given position and radius.
fun cpBBNewForCircle(p: cpVect, r: Float): cpBB {
    return cpBBNewForExtents(p, r, r)
}






