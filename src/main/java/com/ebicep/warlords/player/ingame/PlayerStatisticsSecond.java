package com.ebicep.warlords.player.ingame;

import com.ebicep.warlords.events.player.WarlordsDamageHealingFinalEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerStatisticsSecond implements Iterable<PlayerStatisticsSecond.Entry> {

    private final List<Entry> entries = new ArrayList<>();
    private Entry current = new Entry();

    public PlayerStatisticsSecond() {
        entries.add(current);
    }

    public void advanceSecond() {
        current = new Entry();
        entries.add(current);
    }


    public void addDamageHealingEventAsSelf(WarlordsDamageHealingFinalEvent event) {
        current.eventsAsSelf.add(event);
    }

    public void addDamageHealingEventAsAttacker(WarlordsDamageHealingFinalEvent event) {
        current.eventsAsAttacker.add(event);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public List<WarlordsDamageHealingFinalEvent> getAllEventsAsSelf() {
        List<WarlordsDamageHealingFinalEvent> events = new ArrayList<>();
        entries.forEach(entry -> events.addAll(entry.getEventsAsSelf()));
        return events;
    }

    public List<WarlordsDamageHealingFinalEvent> getAllEventsAsAttacker() {
        List<WarlordsDamageHealingFinalEvent> events = new ArrayList<>();
        entries.forEach(entry -> events.addAll(entry.getEventsAsAttacker()));
        return events;
    }

    public List<WarlordsDamageHealingFinalEvent> getEventsAsSelfFromLastSecond(int seconds) {
        return entries
                .subList(Math.max(0, entries.size() - seconds), entries.size())
                .stream()
                .flatMap(entry -> entry.getEventsAsSelf().stream())
                .collect(Collectors.toList());
    }

    public List<WarlordsDamageHealingFinalEvent> getEventsAsSelfFromLastSecond(int seconds, Predicate<WarlordsDamageHealingFinalEvent> filter) {
        return entries
                .subList(Math.max(0, entries.size() - seconds), entries.size())
                .stream()
                .flatMap(entry -> entry.getEventsAsSelf().stream())
                .filter(filter)
                .collect(Collectors.toList());
    }

    public Stream<WarlordsDamageHealingFinalEvent> getEventsAsSelfFromLastSecondStream(int seconds) {
        return entries
                .subList(Math.max(0, entries.size() - seconds), entries.size())
                .stream()
                .flatMap(entry -> entry.getEventsAsSelf().stream());
    }

    public List<WarlordsDamageHealingFinalEvent> getEventsAsAttackerFromLastSecond(int seconds) {
        return entries
                .subList(Math.max(0, entries.size() - seconds), entries.size())
                .stream()
                .flatMap(entry -> entry.getEventsAsAttacker().stream())
                .collect(Collectors.toList());
    }

    public List<WarlordsDamageHealingFinalEvent> getLastEventsAsSelf(int amount, int timeLimitSeconds) {
        List<WarlordsDamageHealingFinalEvent> events = getEventsAsSelfFromLastSecond(timeLimitSeconds);
        return events.subList(Math.max(0, events.size() - amount), events.size());
    }

    public List<WarlordsDamageHealingFinalEvent> getLastEventsAsSelf(int amount) {
        return getLastEventsAsSelf(amount, 60); //max last 60 seconds
    }

    public WarlordsDamageHealingFinalEvent getLastEventAsSelf() {
        return getLastEventsAsSelf(1, 60).get(0); //max last 60 seconds
    }

    public List<WarlordsDamageHealingFinalEvent> getLastEventsAsAttacker(int amount, int timeLimitSeconds) {
        List<WarlordsDamageHealingFinalEvent> events = getEventsAsAttackerFromLastSecond(timeLimitSeconds);
        return events.subList(Math.max(0, events.size() - amount), events.size());
    }

    public List<WarlordsDamageHealingFinalEvent> getLastEventsAsAttacker(int amount, int timeLimitSeconds, Predicate<WarlordsDamageHealingFinalEvent> filter) {
        List<WarlordsDamageHealingFinalEvent> events = getEventsAsAttackerFromLastSecond(timeLimitSeconds);
        events = events.stream().filter(filter).collect(Collectors.toList());
        return events.subList(Math.max(0, events.size() - amount), events.size());
    }

    public List<WarlordsDamageHealingFinalEvent> getLastEventsAsAttacker(int amount) {
        return getLastEventsAsAttacker(amount, 60); //max last 60 seconds
    }

    public List<WarlordsDamageHealingFinalEvent> getLastEventsAsAttacker(int amount, Predicate<WarlordsDamageHealingFinalEvent> filter) {
        return getLastEventsAsAttacker(amount, 60, filter); //max last 60 seconds
    }

    public WarlordsDamageHealingFinalEvent getLastEventAsAttacker() {
        return getLastEventsAsAttacker(1, 60).get(0); //max last 60 seconds
    }

    @NotNull
    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    public static class Entry {

        private final List<WarlordsDamageHealingFinalEvent> eventsAsSelf = new ArrayList<>();

        private final List<WarlordsDamageHealingFinalEvent> eventsAsAttacker = new ArrayList<>();

        public List<WarlordsDamageHealingFinalEvent> getEventsAsSelf() {
            return eventsAsSelf;
        }

        public List<WarlordsDamageHealingFinalEvent> getEventsAsAttacker() {
            return eventsAsAttacker;
        }


    }

}
