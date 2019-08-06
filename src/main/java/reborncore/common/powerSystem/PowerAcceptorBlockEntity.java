/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.common.powerSystem;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import reborncore.api.IListInfoProvider;
import reborncore.api.power.EnumPowerTier;
import reborncore.api.power.ExternalPowerHandler;
import reborncore.api.power.EnergyBlockEntity;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PowerAcceptorBlockEntity extends MachineBaseBlockEntity implements EnergyBlockEntity, IListInfoProvider // TechReborn
{
	private EnumPowerTier blockEntityPowerTier;
	private double energy;

	public double extraPowerStorage;
	public double extraPowerInput;
	public int extraTier;
	public double powerChange;
	public double powerLastTick;
	public boolean checkOverfill = true; // Set to false to disable the overfill check.
	// Some external power systems (EU) support multiple energy packets per tick,
	// this allows machines to possibly emit
	// multiple packets in a tick. Other power systems such as FE will ignore this
	// option.
	public int maxPacketsPerTick = 1;

	private List<ExternalPowerHandler> powerManagers;

	public PowerAcceptorBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
		checkTier();
		setupManagers();
	}

	private void setupManagers() {
		final PowerAcceptorBlockEntity blockEntity = this;
		powerManagers = ExternalPowerSystems.externalPowerHandlerList.stream()
				.map(externalPowerManager -> externalPowerManager.createPowerHandler(blockEntity)).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void checkTier() {
		if (this.getMaxInput() == 0) {
			blockEntityPowerTier = EnumPowerTier.getTier((int) this.getBaseMaxOutput());
		} else {
			blockEntityPowerTier = EnumPowerTier.getTier((int) this.getBaseMaxInput());
		}

	}

	public void setExtraPowerStorage(double extraPowerStorage) {
		this.extraPowerStorage = extraPowerStorage;
	}

	public void setMaxPacketsPerTick(int maxPacketsPerTick) {
		this.maxPacketsPerTick = maxPacketsPerTick;
	}

	public double getFreeSpace() {
		return getMaxPower() - getEnergy();
	}

	/**
	 * Charge machine from battery placed inside inventory slot
	 *
	 * @param slot int Slot ID for battery slot
	 */
	public void charge(int slot) {
		if (world.isClient) {
			return;
		}

		double chargeEnergy = Math.min(getFreeSpace(), getMaxInput());
		if (chargeEnergy <= 0.0) {
			return;
		}
		if (!getOptionalInventory().isPresent()) {
			return;
		}
		ItemStack batteryStack = getOptionalInventory().get().getInvStack(slot);
		if (batteryStack.isEmpty()) {
			return;
		}

		if (ExternalPowerSystems.isPoweredItem(batteryStack)) {
			ExternalPowerSystems.dischargeItem(this, batteryStack);
		}

	}

	public int getEnergyScaled(int scale) {
		return (int) ((getEnergy() * scale / getMaxPower()));
	}

	public boolean shouldHanldeEnergyNBT() {
		return true;
	}

	public boolean handleTierWithPower() {
		return true;
	}

	public double getPowerChange() {
		return powerChange;
	}

	public void setPowerChange(double powerChange) {
		this.powerChange = powerChange;
	}

	// TileMachineBase
	@Override
	public void tick() {
		super.tick();
		if (world.isClient) {
			return;
		}

		powerManagers.forEach(ExternalPowerHandler::tick);

		powerChange = getEnergy() - powerLastTick;
		powerLastTick = getEnergy();
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		CompoundTag data = tag.getCompound("PowerAcceptor");
		if (shouldHanldeEnergyNBT()) {
			this.setEnergy(data.getDouble("energy"));
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		CompoundTag data = new CompoundTag();
		data.putDouble("energy", getEnergy());
		tag.put("PowerAcceptor", data);
		return tag;
	}

	@Override
	public void resetUpgrades() {
		super.resetUpgrades();
		extraPowerStorage = 0;
		extraTier = 0;
		extraPowerInput = 0;
	}

//	@Override
//	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
//		LazyOptional<T> externalCap = powerManagers.stream()
//			.filter(Objects::nonNull)
//			.map(externalPowerHandler -> externalPowerHandler.getCapability(capability, facing))
//			.filter(LazyOptional::isPresent)
//			.findFirst()
//			.orElse(null);
//
//		if (externalCap != null) {
//			return externalCap;
//		}
//
//		return super.getCapability(capability, facing);
//	}

	public abstract double getBaseMaxPower();

	public abstract double getBaseMaxOutput();

	public abstract double getBaseMaxInput();

	// BlockEntity
	@Override
	public void invalidate() {
		super.invalidate();
		powerManagers.forEach(ExternalPowerHandler::invalidate);
	}

	// @Override
	// TODO 1.13 blockEntity patches are gone?
	public void onChunkUnload() {
		// super.onChunkUnload();

		powerManagers.forEach(ExternalPowerHandler::unload);
	}

	// IEnergyInterfaceTile
	@Override
	public double getEnergy() {
		return energy;
	}

	@Override
	public void setEnergy(double energy) {
		if (!checkOverfill) {
			this.energy = energy;
			return;
		}
		this.energy = Math.max(Math.min(energy, getMaxPower()), 0);
	}

	@Override
	public double addEnergy(double energy) {
		return addEnergy(energy, false);
	}

	@Override
	public double addEnergy(double energy, boolean simulate) {
		double energyReceived = Math.min(getMaxPower(), Math.min(getFreeSpace(), energy));

		if (!simulate) {
			setEnergy(getEnergy() + energyReceived);
		}
		return energyReceived;
	}

	@Override
	public boolean canUseEnergy(double input) {
		return input <= energy;
	}

	@Override
	public double useEnergy(double energy) {
		return useEnergy(energy, false);
	}

	@Override
	public double useEnergy(double extract, boolean simulate) {
		if (extract > energy) {
			extract = energy;
		}
		if (!simulate) {
			setEnergy(energy - extract);
		}
		return extract;
	}

	@Override
	public boolean canAddEnergy(double energyIn) {
		return getEnergy() + energyIn <= getMaxPower();
	}

	@Override
	public double getMaxPower() {
		return getBaseMaxPower() + extraPowerStorage;
	}

	@Override
	public double getMaxOutput() {
		double maxOutput = 0;
		if (this.extraTier > 0) {
			maxOutput = this.getTier().getMaxOutput();
		} else {
			maxOutput = getBaseMaxOutput();
		}
		return maxOutput;
	}

	@Override
	public double getMaxInput() {
		double maxInput = 0;
		if (this.extraTier > 0) {
			maxInput = this.getTier().getMaxInput();
		} else {
			maxInput = getBaseMaxInput();
		}
		return maxInput + extraPowerInput;
	}

	public EnumPowerTier getPushingTier() {
		return getTier();
	}

	@Override
	public EnumPowerTier getTier() {
		if (blockEntityPowerTier == null) {
			checkTier();
		}

		if (extraTier > 0) {
			for (EnumPowerTier enumTier : EnumPowerTier.values()) {
				if (enumTier.ordinal() == blockEntityPowerTier.ordinal() + extraTier) {
					return blockEntityPowerTier;
				}
			}
			return EnumPowerTier.INFINITE;
		}
		return blockEntityPowerTier;
	}

	// IListInfoProvider
	@Override
	public void addInfo(List<Text> info, boolean isReal, boolean hasData) {
		info.add(new LiteralText(Formatting.GRAY + StringUtils.t("reborncore.tooltip.energy.maxEnergy") + ": "
				+ Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted(getMaxPower())));
		if (getMaxInput() != 0) {
			info.add(new LiteralText(Formatting.GRAY + StringUtils.t("reborncore.tooltip.energy.inputRate") + ": "
					+ Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted(getMaxInput())));
		}
		if (getMaxOutput() != 0) {
			info.add(new LiteralText(Formatting.GRAY + StringUtils.t("reborncore.tooltip.energy.outputRate") + ": "
					+ Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted(getMaxOutput())));
		}
		info.add(new LiteralText(Formatting.GRAY + StringUtils.t("reborncore.tooltip.energy.tier") + ": "
				+ Formatting.GOLD + StringUtils.toFirstCapitalAllLowercase(getTier().toString())));
		if (isReal) {
			info.add(new LiteralText(Formatting.GRAY + StringUtils.t("reborncore.tooltip.energy.change") + ": "
					+ Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted(getPowerChange()) + "/t"));
		}

		if (hasData) {
			info.add(new LiteralText(Formatting.GRAY + StringUtils.t("reborncore.tooltip.energy") + ": "
					+ Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted(energy)));
		}

		super.addInfo(info, isReal, hasData);
	}
}