package lumien.bloodmoon.server;

import com.google.common.collect.Sets;
import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents;
import lumien.bloodmoon.config.BloodmoonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Random;
import java.util.Set;

public final class BloodmoonSpawner
{
	private static final int MOB_COUNT_DIV = (int) Math.pow(17.0D, 2.0D);
	private final Set<ChunkPos> eligibleChunksForSpawning = Sets.<ChunkPos> newHashSet();

	private static boolean isNormalCube(BlockState state, Level level, BlockPos pos) {
		return state.getMaterial().isSolid() && !state.isRedstoneConductor(level, pos) && state.getBlock().isOcclusionShapeFullBlock(state, level, pos);
	}

	/**
	 * adds all chunks within the spawn radius of the players to
	 * eligibleChunksForSpawning. pars: the world, hostileCreatures,
	 * passiveCreatures. returns number of eligible chunks.
	 */
	public int findChunksForSpawning(ServerLevel worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate)
	{
		if (!spawnHostileMobs && !spawnPeacefulMobs)
		{
			return 0;
		}
		else
		{
			this.eligibleChunksForSpawning.clear();
			int i = 0;

			for (ServerPlayer entityplayer : worldServerIn.players())
			{
				if (!entityplayer.isSpectator())
				{
					int j = Mth.floor(entityplayer.xo / 16.0D);
					int k = Mth.floor(entityplayer.zo / 16.0D);
					int l = 8;

					for (int i1 = -l; i1 <= l; ++i1)
					{
						for (int j1 = -l; j1 <= l; ++j1)
						{
							boolean flag = i1 == -l || i1 == l || j1 == -l || j1 == l;
							ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

							if (!this.eligibleChunksForSpawning.contains(chunkpos))
							{
								++i;

								if (!flag && worldServerIn.getWorldBorder().isWithinBounds(chunkpos))
								{
									if (!worldServerIn.getChunkSource().chunkMap.getPlayersCloseForSpawning(chunkpos).isEmpty())
									{
										this.eligibleChunksForSpawning.add(chunkpos);
									}
								}
							}
						}
					}
				}
			}

			int j4 = 0;
			BlockPos blockpos1 = worldServerIn.getSharedSpawnPos();

			for (MobCategory category : MobCategory.values())
			{
				if ((!category.isFriendly() || spawnPeacefulMobs) && (category.isFriendly() || spawnHostileMobs) && (!category.isPersistent() || spawnOnSetTickRate))
				{
					int k4 = worldServerIn.getEntities(EntityTypeTest.forClass(Mob.class), (entity) -> entity.getType().getCategory().equals(category)).size();
					int spawnLimit = category.getMaxInstancesPerChunk() * i / MOB_COUNT_DIV;

					spawnLimit *= BloodmoonConfig.SPAWNING.SPAWN_LIMIT_MULT.get();

					if (k4 <= spawnLimit)
					{
						java.util.ArrayList<ChunkPos> shuffled = com.google.common.collect.Lists.newArrayList(this.eligibleChunksForSpawning);
						java.util.Collections.shuffle(shuffled);
						BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
						label415:

						for (ChunkPos chunkcoordintpair1 : shuffled)
						{
							BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunkcoordintpair1.x, chunkcoordintpair1.z);
							int k1 = blockpos.getX();
							int l1 = blockpos.getY();
							int i2 = blockpos.getZ();
							BlockState iblockstate = worldServerIn.getBlockState(blockpos);

							if (!isNormalCube(iblockstate, worldServerIn, blockpos))
							{
								int j2 = 0;

								for (int k2 = 0; k2 < 3; ++k2)
								{
									int l2 = k1;
									int i3 = l1;
									int j3 = i2;
									int k3 = 6;
									MobSpawnSettings.SpawnerData biomegenbase$spawnlistentry = null;
									SpawnGroupData ientitylivingdata = null;
									int l3 = Mth.ceil(Math.random() * 4.0D);

									for (int i4 = 0; i4 < l3; ++i4)
									{
										l2 += worldServerIn.random.nextInt(k3) - worldServerIn.random.nextInt(k3);
										i3 += worldServerIn.random.nextInt(1) - worldServerIn.random.nextInt(1);
										j3 += worldServerIn.random.nextInt(k3) - worldServerIn.random.nextInt(k3);
										blockpos$mutableblockpos.set(l2, i3, j3);
										float f = (float) l2 + 0.5F;
										float f1 = (float) j3 + 0.5F;

										if (worldServerIn.canSeeSky(blockpos$mutableblockpos) && !worldServerIn.hasNearbyAlivePlayer((double) f, (double) i3, (double) f1, BloodmoonConfig.SPAWNING.SPAWN_RANGE.get()) && blockpos1.distToCenterSqr((double) f, (double) i3, (double) f1) >= (BloodmoonConfig.SPAWNING.SPAWN_DISTANCE.get() * BloodmoonConfig.SPAWNING.SPAWN_DISTANCE.get()))
										{
											if (biomegenbase$spawnlistentry == null)
											{
												var spawnListEntry = worldServerIn.getBiome(blockpos$mutableblockpos).value().getMobSettings().getMobs(category).getRandom(worldServerIn.random);

												if (spawnListEntry.isEmpty() || !BloodmoonConfig.canSpawn(spawnListEntry.get().type))
												{
													break;
												}

												biomegenbase$spawnlistentry = spawnListEntry.get();
											}

											var spawnList = worldServerIn.getChunkSource().getGenerator().getMobsAt(worldServerIn.getBiome(blockpos$mutableblockpos), worldServerIn.structureManager(), category, blockpos$mutableblockpos);
											if (
													!spawnList.isEmpty() && spawnList.unwrap().contains(biomegenbase$spawnlistentry)
													&& canCreatureTypeSpawnAtLocation(
															SpawnPlacements
																	.getPlacementType(biomegenbase$spawnlistentry.type),
															worldServerIn,
															blockpos$mutableblockpos
													)
											) {
												Mob entityliving;

												try
												{
													entityliving = (Mob) biomegenbase$spawnlistentry.type.create(worldServerIn);
												}
												catch (Exception exception)
												{
													exception.printStackTrace();
													return j4;
												}

												entityliving.absMoveTo((double) f, (double) i3, (double) f1, worldServerIn.random.nextFloat() * 360.0F, 0.0F);
												var canSpawn = LivingEntityEvents.CHECK_SPAWN.invoker().onCheckSpawn(entityliving, worldServerIn, f, i3, f1, null, MobSpawnType.NATURAL);
												if (canSpawn && (entityliving.checkSpawnObstruction(worldServerIn) && entityliving.checkSpawnRules(worldServerIn, MobSpawnType.NATURAL)))
												{
													//if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityliving, worldServerIn, f, l3, f1))
													//	ientitylivingdata = entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);

													if (entityliving.checkSpawnObstruction(worldServerIn))
													{
														++j2;
														entityliving.getExtraCustomData().putBoolean("bloodmoonSpawned", true);

														worldServerIn.addFreshEntity(entityliving);
													}
													else
													{
														entityliving.remove(Entity.RemovalReason.DISCARDED);
													}

													//if (i2 >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(entityliving))
													//{
													//	continue label415;
													//}
												}

												j4 += j2;
											}
										}
									}
								}
							}
						}
					}
				}
			}

			return j4;
		}
	}

	protected static BlockPos getRandomChunkPosition(Level worldIn, int x, int z)
	{
		LevelChunk chunk = worldIn.getChunk(x, z);
		int i = x * 16 + worldIn.random.nextInt(16);
		int j = z * 16 + worldIn.random.nextInt(16);
		int k = Mth.roundToward(chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j) + 1, 16);
		int l = worldIn.random.nextInt(k > 0 ? k : chunk.getHighestSectionPosition() + 16 - 1);
		return new BlockPos(i, l, j);
	}

	public static boolean func_185331_a(BlockState p_185331_0_, Level level, BlockPos pos)
	{
		return (!p_185331_0_.getMaterial().blocksMotion() || !p_185331_0_.getBlock().isCollisionShapeFullBlock(p_185331_0_, level, pos)) && (p_185331_0_.isSignalSource() ? false : (p_185331_0_.getMaterial().isLiquid() ? false : !BaseRailBlock.isRail(p_185331_0_)));
	}

	public static boolean canCreatureTypeSpawnAtLocation(SpawnPlacements.Type spawnPlacementTypeIn, Level worldIn, BlockPos pos)
	{
		if (!worldIn.getWorldBorder().isWithinBounds(pos))
		{
			return false;
		}
		else
		{
			BlockState iblockstate = worldIn.getBlockState(pos);

			if (spawnPlacementTypeIn == SpawnPlacements.Type.IN_WATER)
			{
				return iblockstate.getMaterial().isLiquid() && worldIn.getBlockState(pos.below()).getMaterial().isLiquid() && !isNormalCube(worldIn.getBlockState(pos.above()), worldIn, pos.above());
			}
			else
			{
				BlockPos blockpos = pos.below();
				BlockState state = worldIn.getBlockState(blockpos);

				if (!state.isFaceSturdy(worldIn, blockpos, Direction.UP))
				{
					return false;
				}
				else
				{
					Block block = worldIn.getBlockState(blockpos).getBlock();
					boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
					return flag && func_185331_a(iblockstate, worldIn, pos) && func_185331_a(worldIn.getBlockState(pos.above()), worldIn, pos.above());
				}
			}
		}
	}

	/**
	 * Called during chunk generation to spawn initial creatures.
	 */
	public static void performWorldGenSpawning(Level worldIn, Biome biomeIn, int p_77191_2_, int p_77191_3_, int p_77191_4_, int p_77191_5_, Random randomIn)
	{
		var randomList = biomeIn.getMobSettings().getMobs(MobCategory.CREATURE);
		List<MobSpawnSettings.SpawnerData> list = randomList.unwrap();

		if (!randomList.isEmpty())
		{
			while (randomIn.nextFloat() < biomeIn.getMobSettings().getCreatureProbability())
			{
				MobSpawnSettings.SpawnerData biomegenbase$spawnlistentry = randomList.getRandom(worldIn.random).get();
				int i = biomegenbase$spawnlistentry.minCount + randomIn.nextInt(1 + biomegenbase$spawnlistentry.maxCount - biomegenbase$spawnlistentry.minCount);
				SpawnGroupData ientitylivingdata = null;
				int j = p_77191_2_ + randomIn.nextInt(p_77191_4_);
				int k = p_77191_3_ + randomIn.nextInt(p_77191_5_);
				int l = j;
				int i1 = k;

				for (int j1 = 0; j1 < i; ++j1)
				{
					boolean flag = false;

					for (int k1 = 0; !flag && k1 < 4; ++k1)
					{
						BlockPos blockpos = worldIn.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(j, 0, k));

						if (canCreatureTypeSpawnAtLocation(SpawnPlacements.Type.ON_GROUND, worldIn, blockpos))
						{
							Mob entityliving;

							try
							{
								entityliving = (Mob) biomegenbase$spawnlistentry.type.create(worldIn);
							}
							catch (Exception exception)
							{
								exception.printStackTrace();
								continue;
							}

							entityliving.absMoveTo((double) ((float) j + 0.5F), (double) blockpos.getY(), (double) ((float) k + 0.5F), randomIn.nextFloat() * 360.0F, 0.0F);
							worldIn.addFreshEntity(entityliving);
							ientitylivingdata = entityliving.finalizeSpawn((ServerLevel) worldIn, worldIn.getCurrentDifficultyAt(entityliving.blockPosition()), MobSpawnType.NATURAL, ientitylivingdata, null);
							flag = true;
						}

						j += randomIn.nextInt(5) - randomIn.nextInt(5);

						for (k += randomIn.nextInt(5) - randomIn.nextInt(5); j < p_77191_2_ || j >= p_77191_2_ + p_77191_4_ || k < p_77191_3_ || k >= p_77191_3_ + p_77191_4_; k = i1 + randomIn.nextInt(5) - randomIn.nextInt(5))
						{
							j = l + randomIn.nextInt(5) - randomIn.nextInt(5);
						}
					}
				}
			}
		}
	}
}