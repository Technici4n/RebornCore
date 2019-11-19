package reborncore.api.praescriptum.fuels;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import reborncore.api.praescriptum.Utils.IngredientUtils;
import reborncore.api.praescriptum.Utils.LogUtils;
import reborncore.api.praescriptum.ingredients.input.FluidStackInputIngredient;
import reborncore.api.praescriptum.ingredients.input.InputIngredient;
import reborncore.api.praescriptum.ingredients.input.ItemStackInputIngredient;
import reborncore.api.praescriptum.ingredients.input.OreDictionaryInputIngredient;
import reborncore.common.util.ItemUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

public class FuelHandler {
    public FuelHandler(String name) {
        if (StringUtils.isBlank(name)) throw new IllegalArgumentException("The fuel handler name cannot be blank");

        this.name = name;
    }

    /**
     * Create a fuel for this handler.
     *
     * @return new fuel object for ease of use
     */
    public Fuel createFuel() {
        return new Fuel(this);
    }

    /**
     * Adds a fuel to this handler.
     *
     * @param fuel    The fuel
     * @param replace Replace conflicting existing fuels, not recommended, may be ignored
     * @return True on success, false otherwise, e.g. on conflicts
     */
    public boolean addFuel(Fuel fuel, boolean replace) {
        Objects.requireNonNull(fuel.getInputIngredients(), "The input is null");

        if (fuel.getInputIngredients().size() <= 0) throw new IllegalArgumentException("No inputs");

        if (fuel.getEnergyOutput() <= 0.0D) throw new IllegalArgumentException("The output is 0");

        ImmutableList<InputIngredient<?>> listOfInputs = fuel.getInputIngredients().stream()
                .filter(IngredientUtils.isIngredientEmpty((ingredient) ->
                        LogUtils.LOGGER.warn(String.format("The %s %s is invalid. Skipping...", ingredient.getClass().getSimpleName(), ingredient.toFormattedString()))))
                .collect(ImmutableList.toImmutableList());

        boolean canBeSkipped = listOfInputs.stream()
                .filter(ingredient -> ingredient instanceof OreDictionaryInputIngredient)
                .anyMatch(ingredient -> OreDictionary.getOres(((OreDictionaryInputIngredient) ingredient).ingredient).isEmpty());

        if (canBeSkipped) {
            LogUtils.LOGGER.warn(String.format("Skipping %s => %s due to the non existence of items that are registered to a provided ore type",
                    listOfInputs, fuel.getEnergyOutput()));

            return false;
        }

        Optional<Fuel> temp = getFuel(listOfInputs);

        if (temp.isPresent()) {
            if (replace) {
                do {
                    if (!removeFuel(listOfInputs))
                        LogUtils.LOGGER.error(String.format("Something went wrong while removing the fuel with inputs %s", listOfInputs));
                } while (getFuel(listOfInputs).isPresent());
            } else {
                LogUtils.LOGGER.error(String.format("Skipping %s => %s due to duplicate input for %s (%s => %s)", listOfInputs,
                        fuel.getEnergyOutput(), listOfInputs, listOfInputs, fuel.getEnergyOutput()));
                return false;
            }
        }

        Fuel newFuel = createFuel()
                .withInput(listOfInputs)
                .withOutput(fuel.getEnergyOutput())
                .withMetadata(fuel.getMetadata());

        fuels.add(newFuel);

        return true;
    }

    /**
     * Get the fuel for the given ingredients.
     *
     * @param ingredients The ingredient list
     * @return The fuel if it exists or empty otherwise
     */
    protected Optional<Fuel> getFuel(ImmutableList<InputIngredient<?>> ingredients) {
        return fuels.stream()
                .filter(fuel -> {
                    final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
                    ingredients.forEach(entry ->
                            listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

                    return listA.isEmpty();
                })
                .findAny();
    }

    /**
     * Find a matching fuel for the provided inputs
     *
     * @param itemStack Fuel input item (not modified)
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findFuel(ItemStack itemStack) {
        if (ItemUtils.isEmpty(itemStack)) return Optional.empty();

        ImmutableList<InputIngredient<?>> ingredients = ImmutableList.of(ItemStackInputIngredient.copyOf(itemStack));
        return Optional.ofNullable(cachedFuels.get(ingredients));
    }

    /**
     * Find a matching fuel for the provided inputs
     *
     * @param itemStacks Fuel input items (not modified)
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findFuel(ImmutableList<ItemStack> itemStacks) {
        ImmutableList<InputIngredient<?>> ingredients = itemStacks.stream()
                .filter(stack -> !ItemUtils.isEmpty(stack))
                .map(ItemStackInputIngredient::copyOf)
                .collect(ImmutableList.toImmutableList());

        return Optional.ofNullable(cachedFuels.get(ingredients));
    }

    /**
     * Find a matching fuel for the provided inputs
     *
     * @param fluidStack Fuel input fluid (not modified)
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findFuel2(FluidStack fluidStack) {
        if (fluidStack.amount <= 0) return Optional.empty();

        ImmutableList<InputIngredient<?>> ingredients = ImmutableList.of(FluidStackInputIngredient.copyOf(fluidStack));
        return Optional.ofNullable(cachedFuels.get(ingredients));
    }

    /**
     * Find a matching fuel for the provided inputs
     *
     * @param fluidStacks Fuel input fluids (not modified)
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findFuel2(ImmutableList<FluidStack> fluidStacks) {
        ImmutableList<InputIngredient<?>> ingredients = fluidStacks.stream()
                .filter(stack -> stack.amount <= 0)
                .map(FluidStackInputIngredient::copyOf)
                .collect(ImmutableList.toImmutableList());

        return Optional.ofNullable(cachedFuels.get(ingredients));
    }

    /**
     * Find a matching fuel for the provided inputs
     *
     * @param itemStacks  Fuel input items (not modified)
     * @param fluidStacks Fuel input fluids (not modified)
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findFuel3(ImmutableList<ItemStack> itemStacks, ImmutableList<FluidStack> fluidStacks) {
        Stream<ItemStackInputIngredient> itemIngredients = itemStacks.stream()
                .filter(stack -> !ItemUtils.isEmpty(stack))
                .map(ItemStackInputIngredient::copyOf); // map ItemStacks

        Stream<FluidStackInputIngredient> fluidIngredients = fluidStacks.stream()
                .filter(stack -> stack.amount <= 0)
                .map(FluidStackInputIngredient::copyOf); // map FluidStacks

        ImmutableList<InputIngredient<?>> ingredients = Stream.concat(itemIngredients, fluidIngredients)
                .collect(ImmutableList.toImmutableList());

        return Optional.ofNullable(cachedFuels.get(ingredients));
    }

    /**
     * Given the inputs find and apply the fuel to said inputs.
     *
     * @param itemStack Fuel input item (not modified)
     * @param simulate  If true the manager will accept partially missing ingredients or
     *                  ingredients with insufficient quantities. This is primarily used to check whether a
     *                  slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findAndApply(ItemStack itemStack, boolean simulate) {
        if (ItemUtils.isEmpty(itemStack)) return Optional.empty();

        ImmutableList<InputIngredient<?>> ingredients = ImmutableList.of(ItemStackInputIngredient.copyOf(itemStack));

        if (ingredients.isEmpty()) return Optional.empty(); // if the inputs are empty we can return nothing

        Optional<Fuel> ret = Optional.ofNullable(cachedFuels.get(ingredients));

        ret.map(fuel -> {
            // check if everything need for the input is available in the input (ingredients + quantities)
            if (ingredients.size() != fuel.getInputIngredients().size()) return Optional.empty();

            final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

            if (!listA.isEmpty()) return Optional.empty(); // the input did not match

            if (!simulate) {
                final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
                ingredients.forEach(entry ->
                        listB.removeIf(temp -> {
                            if (temp.matches(entry.ingredient)) {
                                entry.shrink(temp.getCount()); // adjust the quantity
                                return true;
                            }
                            return false;
                        })
                );
            }

            return Optional.of(fuel);
        });

        return ret;
    }

    /**
     * Given the inputs find and apply the fuel to said inputs.
     *
     * @param itemStacks Fuel input items (not modified)
     * @param simulate   If true the manager will accept partially missing ingredients or
     *                   ingredients with insufficient quantities. This is primarily used to check whether a
     *                   slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findAndApply(ImmutableList<ItemStack> itemStacks, boolean simulate) {
        ImmutableList<InputIngredient<?>> ingredients = itemStacks.stream()
                .filter(stack -> !ItemUtils.isEmpty(stack))
                .map(ItemStackInputIngredient::copyOf)
                .collect(ImmutableList.toImmutableList());

        if (ingredients.isEmpty()) return Optional.empty(); // if the inputs are empty we can return nothing

        Optional<Fuel> ret = Optional.ofNullable(cachedFuels.get(ingredients));

        ret.map(fuel -> {
            // check if everything need for the input is available in the input (ingredients + quantities)
            if (ingredients.size() != fuel.getInputIngredients().size()) return Optional.empty();

            final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

            if (!listA.isEmpty()) return Optional.empty(); // the input did not match

            if (!simulate) {
                final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
                ingredients.forEach(entry ->
                        listB.removeIf(temp -> {
                            if (temp.matches(entry.ingredient)) {
                                entry.shrink(temp.getCount()); // adjust the quantity
                                return true;
                            }
                            return false;
                        })
                );
            }

            return Optional.of(fuel);
        });

        return ret;
    }

    /**
     * Given the inputs find and apply the fuel to said inputs.
     *
     * @param fluidStack Fuel input fluid (not modified)
     * @param simulate   If true the manager will accept partially missing ingredients or
     *                   ingredients with insufficient quantities. This is primarily used to check whether a
     *                   slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findAndApply2(FluidStack fluidStack, boolean simulate) {
        if (fluidStack.amount <= 0) return Optional.empty();

        ImmutableList<InputIngredient<?>> ingredients = ImmutableList.of(FluidStackInputIngredient.copyOf(fluidStack));

        if (ingredients.isEmpty()) return Optional.empty(); // if the inputs are empty we can return nothing

        Optional<Fuel> ret = Optional.ofNullable(cachedFuels.get(ingredients));

        ret.map(fuel -> {
            // check if everything need for the input is available in the input (ingredients + quantities)
            if (ingredients.size() != fuel.getInputIngredients().size()) return Optional.empty();

            final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

            if (!listA.isEmpty()) return Optional.empty(); // the input did not match

            if (!simulate) {
                final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
                ingredients.forEach(entry ->
                        listB.removeIf(temp -> {
                            if (temp.matches(entry.ingredient)) {
                                entry.shrink(temp.getCount()); // adjust the quantity
                                return true;
                            }
                            return false;
                        })
                );
            }

            return Optional.of(fuel);
        });

        return ret;
    }

    /**
     * Given the inputs find and apply the fuel to said inputs.
     *
     * @param fluidStacks Fuel input fluids (not modified)
     * @param simulate    If true the manager will accept partially missing ingredients or
     *                    ingredients with insufficient quantities. This is primarily used to check whether a
     *                    slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findAndApply2(ImmutableList<FluidStack> fluidStacks, boolean simulate) {
        ImmutableList<InputIngredient<?>> ingredients = fluidStacks.stream()
                .filter(stack -> stack.amount <= 0)
                .map(FluidStackInputIngredient::copyOf)
                .collect(ImmutableList.toImmutableList());

        if (ingredients.isEmpty()) return Optional.empty(); // if the inputs are empty we can return nothing

        Optional<Fuel> ret = Optional.ofNullable(cachedFuels.get(ingredients));

        ret.map(fuel -> {
            // check if everything need for the input is available in the input (ingredients + quantities)
            if (ingredients.size() != fuel.getInputIngredients().size()) return Optional.empty();

            final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

            if (!listA.isEmpty()) return Optional.empty(); // the input did not match

            if (!simulate) {
                final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
                ingredients.forEach(entry ->
                        listB.removeIf(temp -> {
                            if (temp.matches(entry.ingredient)) {
                                entry.shrink(temp.getCount()); // adjust the quantity
                                return true;
                            }
                            return false;
                        })
                );
            }

            return Optional.of(fuel);
        });

        return ret;
    }

    /**
     * Given the inputs find and apply the fuel to said inputs.
     *
     * @param itemStacks  Fuel input items (not modified)
     * @param fluidStacks Fuel input fluids (not modified)
     * @param simulate    If true the manager will accept partially missing ingredients or
     *                    ingredients with insufficient quantities. This is primarily used to check whether a
     *                    slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return Fuel result, or empty if none
     */
    public Optional<Fuel> findAndApply3(ImmutableList<ItemStack> itemStacks, ImmutableList<FluidStack> fluidStacks, boolean simulate) {
        Stream<ItemStackInputIngredient> itemIngredients = itemStacks.stream()
                .filter(stack -> !ItemUtils.isEmpty(stack))
                .map(ItemStackInputIngredient::of); // map ItemStacks

        Stream<FluidStackInputIngredient> fluidIngredients = fluidStacks.stream()
                .filter(stack -> stack.amount <= 0)
                .map(FluidStackInputIngredient::of); // map FluidStacks

        ImmutableList<InputIngredient<?>> ingredients = Stream.concat(itemIngredients, fluidIngredients)
                .collect(ImmutableList.toImmutableList());

        if (ingredients.isEmpty()) return Optional.empty(); // if the inputs are empty we can return nothing

        Optional<Fuel> ret = Optional.ofNullable(cachedFuels.get(ingredients));

        ret.map(fuel -> {
            // check if everything need for the input is available in the input (ingredients + quantities)
            if (ingredients.size() != fuel.getInputIngredients().size()) return Optional.empty();

            final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

            if (!listA.isEmpty()) return Optional.empty(); // the input did not match

            if (!simulate) {
                final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
                ingredients.forEach(entry ->
                        listB.removeIf(temp -> {
                            if (temp.matches(entry.ingredient)) {
                                entry.shrink(temp.getCount()); // adjust the quantity
                                return true;
                            }
                            return false;
                        })
                );
            }

            return Optional.of(fuel);
        });

        return ret;
    }

    /**
     * Given the inputs and the fuel apply the fuel to said inputs.
     *
     * @param fuel      The fuel
     * @param itemStack Fuel input item (not modified)
     * @param simulate  If true the manager will accept partially missing ingredients or
     *                  ingredients with insufficient quantities. This is primarily used to check whether a
     *                  slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return True if the operation was successful or false otherwise
     */
    public boolean apply(Fuel fuel, ItemStack itemStack, boolean simulate) {
        if (ItemUtils.isEmpty(itemStack)) return false;

        ImmutableList<InputIngredient<?>> ingredients = ImmutableList.of(ItemStackInputIngredient.copyOf(itemStack));

        // check if everything need for the input is available in the input (ingredients + quantities)
        if (ingredients.size() != fuel.getInputIngredients().size()) return false;

        final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
        ingredients.forEach(entry ->
                listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

        if (!listA.isEmpty()) return false; // the input did not match

        if (!simulate) {
            final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listB.removeIf(temp -> {
                        if (temp.matches(entry.ingredient)) {
                            entry.shrink(temp.getCount()); // adjust the quantity
                            return true;
                        }
                        return false;
                    })
            );
        }

        return true;
    }

    /**
     * Given the inputs and the fuel apply the fuel to said inputs.
     *
     * @param fuel       The fuel
     * @param itemStacks Fuel input items (not modified)
     * @param simulate   If true the manager will accept partially missing ingredients or
     *                   ingredients with insufficient quantities. This is primarily used to check whether a
     *                   slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return True if the operation was successful or false otherwise
     */
    public boolean apply(Fuel fuel, ImmutableList<ItemStack> itemStacks, boolean simulate) {
        ImmutableList<InputIngredient<?>> ingredients = itemStacks.stream()
                .filter(stack -> !ItemUtils.isEmpty(stack))
                .map(ItemStackInputIngredient::of)
                .collect(ImmutableList.toImmutableList());

        // check if everything need for the input is available in the input (ingredients + quantities)
        if (ingredients.size() != fuel.getInputIngredients().size()) return false;

        final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
        ingredients.forEach(entry ->
                listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

        if (!listA.isEmpty()) return false; // the input did not match

        if (!simulate) {
            final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listB.removeIf(temp -> {
                        if (temp.matches(entry.ingredient)) {
                            entry.shrink(temp.getCount()); // adjust the quantity
                            return true;
                        }
                        return false;
                    })
            );
        }

        return true;
    }

    /**
     * Given the inputs and the fuel apply the fuel to said inputs.
     *
     * @param fuel       The fuel
     * @param fluidStack Fuel input fluid (not modified)
     * @param simulate   If true the manager will accept partially missing ingredients or
     *                   ingredients with insufficient quantities. This is primarily used to check whether a
     *                   slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return True if the operation was successful or false otherwise
     */
    public boolean apply2(Fuel fuel, FluidStack fluidStack, boolean simulate) {
        if (fluidStack.amount <= 0) return false;

        ImmutableList<InputIngredient<?>> ingredients = ImmutableList.of(FluidStackInputIngredient.copyOf(fluidStack));

        // check if everything need for the input is available in the input (ingredients + quantities)
        if (ingredients.size() != fuel.getInputIngredients().size()) return false;

        final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
        ingredients.forEach(entry ->
                listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

        if (!listA.isEmpty()) return false; // the input did not match

        if (!simulate) {
            final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listB.removeIf(temp -> {
                        if (temp.matches(entry.ingredient)) {
                            entry.shrink(temp.getCount()); // adjust the quantity
                            return true;
                        }
                        return false;
                    })
            );
        }

        return true;
    }

    /**
     * Given the inputs and the fuel apply the fuel to said inputs.
     *
     * @param fuel        The fuel
     * @param fluidStacks Fuel input fluids (not modified)
     * @param simulate    If true the manager will accept partially missing ingredients or
     *                    ingredients with insufficient quantities. This is primarily used to check whether a
     *                    slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return True if the operation was successful or false otherwise
     */
    public boolean apply2(Fuel fuel, ImmutableList<FluidStack> fluidStacks, boolean simulate) {
        ImmutableList<InputIngredient<?>> ingredients = fluidStacks.stream()
                .filter(stack -> stack.amount <= 0)
                .map(FluidStackInputIngredient::copyOf)
                .collect(ImmutableList.toImmutableList());

        // check if everything need for the input is available in the input (ingredients + quantities)
        if (ingredients.size() != fuel.getInputIngredients().size()) return false;

        final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
        ingredients.forEach(entry ->
                listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

        if (!listA.isEmpty()) return false; // the input did not match

        if (!simulate) {
            final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listB.removeIf(temp -> {
                        if (temp.matches(entry.ingredient)) {
                            entry.shrink(temp.getCount()); // adjust the quantity
                            return true;
                        }
                        return false;
                    })
            );
        }

        return true;
    }

    /**
     * Given the inputs and the fuel apply the fuel to said inputs.
     *
     * @param fuel        The fuel
     * @param itemStacks  Fuel input items (not modified)
     * @param fluidStacks Fuel input fluids (not modified)
     * @param simulate    If true the manager will accept partially missing ingredients or
     *                    ingredients with insufficient quantities. This is primarily used to check whether a
     *                    slot/tank/etc can accept the input while trying to supply a machine with resources
     * @return True if the operation was successful or false otherwise
     */
    public boolean apply3(Fuel fuel, ImmutableList<ItemStack> itemStacks, ImmutableList<FluidStack> fluidStacks, boolean simulate) {
        Stream<ItemStackInputIngredient> itemIngredients = itemStacks.stream()
                .filter(stack -> !ItemUtils.isEmpty(stack))
                .map(ItemStackInputIngredient::of); // map ItemStacks

        Stream<FluidStackInputIngredient> fluidIngredients = fluidStacks.stream()
                .filter(stack -> stack.amount <= 0)
                .map(FluidStackInputIngredient::of); // map FluidStacks

        ImmutableList<InputIngredient<?>> ingredients = Stream.concat(itemIngredients, fluidIngredients)
                .collect(ImmutableList.toImmutableList());

        // check if everything need for the input is available in the input (ingredients + quantities)
        if (ingredients.size() != fuel.getInputIngredients().size()) return false;

        final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
        ingredients.forEach(entry ->
                listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

        if (!listA.isEmpty()) return false; // the input did not match

        if (!simulate) {
            final List<InputIngredient<?>> listB = new ArrayList<>(fuel.getInputIngredients());
            ingredients.forEach(entry ->
                    listB.removeIf(temp -> {
                        if (temp.matches(entry.ingredient)) {
                            entry.shrink(temp.getCount()); // adjust the quantity
                            return true;
                        }
                        return false;
                    })
            );
        }

        return true;
    }

    /**
     * Removes a fuel from this handler.
     *
     * @param fuel The fuel
     * @return True if the fuel has been removed or false otherwise
     */
    public boolean removeFuel(Fuel fuel) {
        if (fuel == null) return false;

        cachedFuels.invalidate(fuel); // remove from cache
        return fuels.remove(fuel);
    }

    /**
     * Removes a fuel from this handler.
     *
     * @param ingredients The input ingredients
     * @return True if a valid fuel has been found and removed or false otherwise
     */
    public boolean removeFuel(ImmutableList<InputIngredient<?>> ingredients) {
        Fuel fuel = getFuel(ingredients).orElse(null);
        if (fuel == null) return false;

        cachedFuels.invalidate(ingredients); // remove from cache
        return fuels.remove(fuel);
    }

    /**
     * Get all the fuels from this handler
     *
     * @return A list with all the fuels
     */
    public List<Fuel> getFuels() {
        return fuels;
    }

    // Fields >>
    public final String name;

    protected final List<Fuel> fuels = new ArrayList<>();

    protected final LoadingCache<ImmutableList<InputIngredient<?>>, Fuel> cachedFuels =
            Caffeine.newBuilder()
                    .expireAfterAccess(10, TimeUnit.MINUTES)
                    .maximumSize(100)
                    .build(ingredients ->
                            fuels.stream()
                                    .filter(fuel -> {
                                        final List<InputIngredient<?>> listA = new ArrayList<>(fuel.getInputIngredients());
                                        ingredients.forEach(entry ->
                                                listA.removeIf(temp -> temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()));

                                        return listA.isEmpty();
                                    })
                                    .findAny()
                                    .orElse(null)
                    );
    // << Fields
}