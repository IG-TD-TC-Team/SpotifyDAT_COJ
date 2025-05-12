package cache;

import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Song;
import songsAndArtists.Genre;
import java.util.List;
import java.util.Optional;

public class CachedSongRepositoryWrapper implements SongRepositoryInterface {
    private final CachedRepository<Song> cachedRepo;

    public CachedSongRepositoryWrapper(CachedRepository<Song> cachedRepo) {
        this.cachedRepo = cachedRepo;
    }

    // Basic CRUD operations delegate to CachedRepository
    @Override
    public List<Song> findAll() {
        return cachedRepo.findAll();
    }

    @Override
    public Optional<Song> findById(int id) {
        return cachedRepo.findById(id);
    }

    @Override
    public Song save(Song song) {
        return cachedRepo.save(song);
    }

    @Override
    public void saveAll(List<Song> songs) {
        cachedRepo.saveAll(songs);
    }

    @Override
    public Optional<Song> update(Song song) {
        return cachedRepo.update(song);
    }

    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    // Special methods: for now, filter cached data
    // This is a simple approach - you might optimize later
    @Override
    public List<Song> findByArtistId(int artistId) {
        return findAll().stream()
                .filter(song -> song.getArtistId() == artistId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Song> findByGenre(Genre genre) {
        return findAll().stream()
                .filter(song -> song.getGenre() == genre)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Song> findByTitle(String title) {
        return findAll().stream()
                .filter(song -> song.getTitle().equalsIgnoreCase(title))
                .collect(java.util.stream.Collectors.toList());
    }
}