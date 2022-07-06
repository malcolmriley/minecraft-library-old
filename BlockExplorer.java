package paragon.minecraft.library;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import paragon.minecraft.wilytextiles.internal.Utilities;

/**
 * Class for the exploration/evaluation of {@link BlockPos}.
 * <p>
 * Individual {@link BlockPos} are incrementally explored by calls to {@link #explore(int)}
 *
 * @author Malcolm Riley
 */
public abstract class BlockExplorer {

	/* Internal Fields */
	private final Queue<BlockPos> EXPLORE_QUEUE = new LinkedList<>();
	private final Set<BlockPos> EXPLORED = new HashSet<>();
	
	/* Constants */
	private static final String EXCEPTION_NULL_BLOCKPOS = "Cannot enqueue null BlockPos.";
	
	/**
	 * Constructs a {@link MatchingBlockExplorer} that will explore contiguous blocks passing the provided {@link Predicate}.
	 * 
	 * @param world - A reference to the {@link IWorld} to explore in
	 * @param stateEvaluator - A {@link BlockState} evaluator
	 * @return A suitable {@link MatchingBlockExplorer}.
	 */
	public static final MatchingBlockExplorer contiguousMatching(Level world, Predicate<BlockState> stateEvaluator) {
		MatchingBlockExplorer instance = new MatchingBlockExplorer();
		instance.setExploreFunction(Utilities.Game::orthogonalNeighborsOf);
		instance.setEvaluator(position -> Utilities.Game.isBlockLoaded(world, position) && stateEvaluator.test(world.getBlockState(position)));
		return instance;
	}

	/**
	 * Constructs a {@link MatchingBlockExplorer} that will explore contiguous blocks. The evaluator function must still be suppplied by a call to {@link MatchingBlockExplorer#setEvaluator(Predicate)}.
	 * 
	 * @param world - A reference to the {@link IWorld} to explore in
	 * @return A suitable {@link MatchingBlockExplorer} that will explore contiguous blocks.
	 */
	public static final MatchingBlockExplorer contiguous() {
		MatchingBlockExplorer instance = new MatchingBlockExplorer();
		instance.setExploreFunction(Utilities.Game::orthogonalNeighborsOf);
		return instance;
	}

	/**
	 * Returns an {@link Stream} over all {@link BlockPos} already explored by this {@link BlockExplorer}.
	 *
	 * @return A {@link Stream} of {@link BlockPos} already explored by this {@link BlockExplorer}.
	 */
	public final Stream<BlockPos> streamExplored() {
		return this.EXPLORED.stream();
	}

	/**
	 * Returns an {@link Iterable} over all {@link BlockPos} already explored by this {@link BlockExplorer}.
	 *
	 * @return An {@link Iterable} of {@link BlockPos} already explored by this {@link BlockExplorer}.
	 */
	public final Iterable<BlockPos> iterateExplored() {
		return this.EXPLORED;
	}

	/**
	 * Returns the count of unique explored {@link BlockPos}.
	 *
	 * @return The number of unique {@link BlockPos} explored.
	 */
	public final int countExplored() {
		return this.EXPLORED.size();
	}

	/**
	 * Returns whether this {@link BlockExplorer} has already explored the passed {@link BlockPos}.
	 *
	 * @param position - A {@link BlockPos}
	 * @return Whether the passed {@link BlockPos} has been explored.
	 */
	public final boolean hasExplored(BlockPos position) {
		return this.EXPLORED.contains(position);
	}

	/**
	 * Returns whether or not this {@link BlockExplorer} has any further explorations to perform.
	 *
	 * @return Whether there is more exploring to be done.
	 */
	public boolean isFinishedExploring() {
		return this.EXPLORE_QUEUE.isEmpty();
	}

	/**
	 * Enqueues the passed {@link BlockPos} for exploration without regard to previous exploration/evaluation.
	 * <p>
	 * Prefer use of {@link #enqueueIfUnexplored(BlockPos)}, as use of this method can mean that positions get explored more than once.
	 *
	 * @param position - The {@link BlockPos} to enqueue for exploration and evaluation.
	 * @return Whether the passed {@link BlockPos} was successfully enqueued.
	 */
	public boolean enqueue(@Nonnull BlockPos position) {
		return this.EXPLORE_QUEUE.add(Objects.requireNonNull(position, EXCEPTION_NULL_BLOCKPOS));
	}
	
	/**
	 * Convenience method to restart this {@link BlockExplorer} using the passed {@link BlockPos} as a starting point.
	 * <p>
	 * Internally, simply calls {@link #clear()} and then {@link #enqueue(BlockPos)}.
	 * 
	 * @param position - The position to begin exploration
	 * @return {@code this}, for convenient method chaining.
	 */
	public BlockExplorer restartWith(@Nonnull BlockPos position) {
		this.clear();
		this.enqueue(position);
		return this;
	}

	/**
	 * Enqueues the passed {@link BlockPos} for exploration if and only if the {@link BlockPos} has not already
	 * been explored since instantiation (or the last call to {@link #clear()}.
	 * <p>
	 * This method returns TRUE if the passed {@link BlockPos} was enqueued, and FALSE otherwise.
	 *
	 * @param position - The {@link BlockPos} to enqueue for exploration and evaluation.
	 * @return Whether the passed {@link BlockPos} was successfully enqueued.
	 */
	public boolean enqueueIfUnexplored(@Nonnull BlockPos position) {
		if (!this.hasExplored(position)) {
			return this.enqueue(position);
		}
		return false;
	}

	/**
	 * Explores exactly one {@link BlockPos} from the internal exploration queue.
	 * <p>
	 * If there are no {@link BlockPos} in the interanl exploration queue, this method will do nothing.
	 * @return {@code this}, for convenient method chaining.
	 */
	public final BlockExplorer exploreSingle() {
		return this.explore(1);
	}

	/**
	 * Explores all possible {@link BlockPos} from the internal exploration queue.
	 * <p>
	 * Use EXTREME CAUTION when calling this method, as exploration itself has the potential to add additional {@link BlockPos} to the internal exploration;
	 * this method will continue to explore and enqueue new positions until the exploration queue is fully exhausted.
	 * <p>
	 * If there are no {@link BlockPos} in the internal exploration queue, this method will do nothing.
	 * @return {@code this}, for convenient method chaining.
	 */
	public final BlockExplorer exploreAll() {
		return this.explore(Integer.MAX_VALUE);
	}

	/**
	 * Explores up to {@code max} quantity of {@link BlockPos} from the internal exploration queue.
	 * <p>
	 * Additional {@link BlockPos} may or may not be added to the queue.
	 *
	 * @param max - The maximum number of {@link BlockPos} to explore in this cycle.
	 * @return {@code this}, for convenient method chaining.
	 */
	public final BlockExplorer explore(int max) {
		for (int count = 0; this.shouldContinueExploring(count, max); count += 1) {
			BlockPos next = this.EXPLORE_QUEUE.poll();
			this.evaluate(next);
			this.EXPLORED.add(next);
		}
		return this;
	}

	/**
	 * Clears the internal exploration queue and record of explord {@link BlockPos}.
	 * @return {@code this}, for convenient method chaining.
	 */
	public BlockExplorer clear() {
		this.EXPLORE_QUEUE.clear();
		this.EXPLORED.clear();
		return this;
	}

	/* Abstract Methods */

	/**
	 * This method should be used to evaluate the passed {@link BlockPos} using the passed {@link IWorld} reference.
	 * <p>
	 * Implementors will ideally follow these general guidelines:
	 * <li>Implementing subclasses should not try to evaluate more than the single {@link BlockPos} passed to this method.</li>
	 * <li>Implementing subclasses should also use this method to enqueue blocks for future exploration, using {@link #enqueue(BlockPos)} and {@link #enqueueIfUnexplored(BlockPos)}.</li>
	 *
	 * @param position - The {@link BlockPos} to explore and evaluate.
	 */
	protected abstract void evaluate(final BlockPos position);

	/* Internal Methods */

	/**
	 * This method should return whether exploration and evaluation should continue. It is called at least once per call to {@link #explore(IWorld, int)}
	 * and is used to determine whether further {@link BlockPos} should be explored.
	 * <p>
	 * Overriding subclasses should call {@code super} for convenience, as this method checks for the emptiness of the exploration queue as well as the count against the max requested.
	 *
	 * @param count - The number of {@link BlockPos} explored so far this call to {@link #explore(IWorld, int)}
	 * @param max - The requested maximum quantity of {@link BlockPos} to explore this call to {@link #explore(IWorld, int)}
	 * @return Whether exploration should continue for this call to {@link #explore(IWorld, int)}.
	 */
	protected boolean shouldContinueExploring(int count, int max) {
		return !this.isFinishedExploring() && (count < max);
	}

	/**
	 * Specialized {@link BlockExplorer} that explores positions based on a provided {@link Predicate} and exploration {@link Function}.
	 * <p>
	 * If the explored {@link BlockPos} passes the {@link Predicate#test(BlockPos)} method of the passed {@link Predicate}, it will be added
	 * to an internal collection of matches and future explorations will be queued based on the provided exploration {@link Function}.
	 * <p>
	 * These matching {@link BlockPos} can be recalled at any time (whether or not exploration has completed) via {@link #streamExplored()} and {@link #iterateExplored()}.
	 * <p>
	 * Calls to the "explore" family will not fail if a valid evaluation {@link Predicate} and exploration {@link Function} have not been set via {@link #setEvaluator(Predicate)}
	 * and {@link #setExploreFunction(Function)}, but calls to these methods should be made with non-{@code null} objects.
	 *
	 * @author Malcolm Riley
	 */
	public static class MatchingBlockExplorer extends BlockExplorer {

		/* Internal Methods */
		private final Set<BlockPos> MATCHES = new HashSet<>();
		private Predicate<BlockPos> EVALUATOR = Predicates.alwaysFalse();
		private Function<BlockPos, Iterable<BlockPos>> EXPLORER = pos -> Collections.emptyList();
		
		/* Constants */
		private static final String EXCEPTION_NULL_EVALUATOR = "BlockPos predicate cannot be null.";
		private static final String EXCEPTION_NULL_EXPLORER = "Exploration function cannot be null.";

		/* Public Methods */

		/**
		 * Returns a {@link Stream} over all matches discovered by this {@link MatchingBlockExplorer}.
		 *
		 * @return A {@link Stream} of all matches discovered by this {@link MatchingBlockExplorer}.
		 */
		public Stream<BlockPos> streamMatching() {
			return this.MATCHES.stream();
		}

		/**
		 * Returns an {@link Iterable} over all matches discovered by this {@link MatchingBlockExplorer}.
		 *
		 * @return An {@link Iterable} over all matches discovered by this {@link MatchingBlockExplorer}.
		 */
		public Iterable<BlockPos> iterateMatching() {
			return this.MATCHES;
		}

		/**
		 * Returns whether any matching {@link BlockPos} have been discovered by this {@link MatchingBlockExplorer}.
		 *
		 * @return Whether any matching {@link BlockPos} have been discovered so far.
		 */
		public boolean hasAnyMatching() {
			return !this.MATCHES.isEmpty();
		}

		/**
		 * Returns the count of matching {@link BlockPos} discovered by this {@link MatchingBlockExplorer}.
		 *
		 * @return The quantity of matching {@link BlockPos} disovered so far.
		 */
		public int countMatching() {
			return this.MATCHES.size();
		}

		/**
		 * Sets the {@link BlockPos} evaluation {@link Predicate} to be used for evaluating {@link BlockPos}.
		 * <p>
		 * The passed {@link Predicate} must be non-null.
		 *
		 * @param evaluator - The evaluator to use
		 */
		public void setEvaluator(@Nonnull Predicate<BlockPos> evaluator) {
			this.EVALUATOR = Objects.requireNonNull(evaluator, EXCEPTION_NULL_EVALUATOR);
		}

		/**
		 * Sets the exploration {@link Function} to be used when queueing future {@link BlockPos} for exploration.
		 * <p>
		 * The passed {@link Function} will be queried to provide an {@link Iterable<BlockPos>} of all future {@link BlockPos} that should be
		 * enqueued for exploration given the presently-explored {@link BlockPos}.
		 * <p>
		 * The passed {@link Function} must be non-null.
		 *
		 * @param exploreFunction
		 */
		public void setExploreFunction(@Nonnull Function<BlockPos, Iterable<BlockPos>> exploreFunction) {
			this.EXPLORER = Objects.requireNonNull(exploreFunction, EXCEPTION_NULL_EXPLORER);
		}

		/* Supertype Override Methods */

		@Override
		protected void evaluate(BlockPos position) {
			if (this.EVALUATOR.test(position) && this.MATCHES.add(position)) {
				this.EXPLORER.apply(position).forEach(this::enqueueIfUnexplored);
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Also clears the internal list of disovered matches.
		 */
		@Override
		public BlockExplorer clear() {
			super.clear();
			this.MATCHES.clear();
			return this;
		}

	}

}
